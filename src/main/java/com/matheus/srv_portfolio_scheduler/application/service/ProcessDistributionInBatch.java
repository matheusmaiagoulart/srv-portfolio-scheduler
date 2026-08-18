package com.matheus.srv_portfolio_scheduler.application.service;

import com.matheus.srv_portfolio_scheduler.domain.services.dto.ResidualsFromMaster;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.CustodyRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.CustomerRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.DedoDuroOutboxRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.DeliveryRepositoryPort;
import com.matheus.srv_portfolio_scheduler.domain.entities.BrokerageAccount;
import com.matheus.srv_portfolio_scheduler.domain.entities.Custody;
import com.matheus.srv_portfolio_scheduler.domain.entities.DedoDuroOutbox;
import com.matheus.srv_portfolio_scheduler.domain.entities.PurchaseOrder;
import com.matheus.srv_portfolio_scheduler.domain.events.IRDedoDuroEvent;
import com.matheus.srv_portfolio_scheduler.domain.services.IRDedoDuroCalculator;
import com.matheus.srv_portfolio_scheduler.domain.services.PortfolioDistribution;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.*;
import com.matheus.srv_portfolio_scheduler.domain.services.rebalance.IndividualDistributionOperation;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessDistributionInBatch {

    private final CustodyRepositoryPort custodyRepository;
    private final IRDedoDuroCalculator irDedoDuroCalculator;
    private final CustomerRepositoryPort customerRepository;
    private final DeliveryRepositoryPort deliveryRepository;
    private final PortfolioDistribution portfolioDistribution;
    private final IRDedoDuroOutboxService irDedoDuroOutboxService;
    private final DedoDuroOutboxRepositoryPort dedoDuroOutboxRepository;
    private final IndividualDistributionOperation individualDistributionOperation;

    @Value("${app.customers_batch_size}")
    private int BATCH_SIZE;

    @Transactional
    public PurchaseSummaryDTO processInBatch(List<PurchaseOrder> purchaseOrders, Money thirdValue, BrokerageAccount masterAccount) {

        log.info("Starting process of distribution");

        long lastId = 0;
        int totalCustomersProcessed = 0;
        int totalDeliveries = 0;
        int totalOutboxEntries = 0;
        List<PurchaseOrdersPerAsset> purchaseOrdersPerAssets = new ArrayList<>();

        long startTime = System.currentTimeMillis();
        updateMasterWithPurchaseQuantity(masterAccount, purchaseOrders);
        Map<String, TickerData> fixedTotalPerTicker = portfolioDistribution.buildInitialTotalPerTicker(purchaseOrders, masterAccount);

        while (true) {

            Map<Long, CustodyPurchaseDataDTO> customersChunk = customerRepository.getChunkOfCustomers(lastId, BATCH_SIZE);
            if (customersChunk.isEmpty()) break;

            PurchaseRoundDataDTO purchaseRoundData = new PurchaseRoundDataDTO(thirdValue, customersChunk);
            DistributionsResultDTO chunkResult = portfolioDistribution.distribute(purchaseOrders, purchaseRoundData, masterAccount, fixedTotalPerTicker);

            deliveryRepository.saveAll(chunkResult.deliveries());
            custodyRepository.saveAll(chunkResult.modifiedCustodies());

            List<IRDedoDuroEvent> irDedoDuroList = irDedoDuroCalculator.calculate(chunkResult.deliveries(), customersChunk);
            List<DedoDuroOutbox> outboxEntries = irDedoDuroOutboxService.createOutboxEntries(
                    irDedoDuroList.stream()
                            .map(IRDedoDuroEvent::toString)
                            .toList());

            dedoDuroOutboxRepository.saveAll(outboxEntries);

            totalCustomersProcessed += customersChunk.size();
            totalDeliveries += chunkResult.deliveries().size();
            totalOutboxEntries += outboxEntries.size();

            if (purchaseOrdersPerAssets.isEmpty())
                purchaseOrdersPerAssets.addAll(chunkResult.purchaseOrdersPerAssets());

            if (customersChunk.size() < BATCH_SIZE) break;

            lastId = customersChunk.keySet().stream()
                    .max(Long::compareTo)
                    .orElse(lastId);
        }

        custodyRepository.saveAll(masterAccount.getCustodies());

        long elapsedTime = System.currentTimeMillis() - startTime;
        log.info("Batch processing completed in {}ms ({} seconds)", elapsedTime, elapsedTime / 1000.0);

        return new PurchaseSummaryDTO(
                purchaseOrdersPerAssets,
                getResidualsFromMaster(masterAccount),
                totalCustomersProcessed,
                totalDeliveries,
                totalOutboxEntries);
    }

    /**
     * Variante do processInBatch usada no fluxo de rebalanceamento: em vez de
     * distribuir proporcionalmente ao saldo do cliente no pool total, entrega
     * a cada cliente exatamente a quantidade calculada como sua necessidade
     * individual (RebalanceExecutionResultDTO), respeitando o limite de
     * ações disponíveis na master.
     */
    @Transactional
    public PurchaseSummaryDTO processIndividualDistribution(
            List<PurchaseOrder> purchaseOrders,
            RebalanceExecutionResultDTO rebalanceResult,
            BrokerageAccount masterAccount) {

        log.info("Starting process of individual distribution (rebalance)");

        long lastId = 0;
        int totalCustomersProcessed = 0;
        int totalDeliveries = 0;
        int totalOutboxEntries = 0;
        List<PurchaseOrdersPerAsset> purchaseOrdersPerAssets = new ArrayList<>();

        Map<Long, CustomerBuyNeedDTO> buyNeedsByCustomer = rebalanceResult.byCustomerId();

        long startTime = System.currentTimeMillis();
        updateMasterWithPurchaseQuantity(masterAccount, purchaseOrders);
        Map<String, TickerData> fixedTotalPerTicker = portfolioDistribution.buildInitialTotalPerTicker(purchaseOrders, masterAccount);

        while (true) {

            Map<Long, CustodyPurchaseDataDTO> customersChunk = customerRepository.getChunkOfCustomers(lastId, BATCH_SIZE);
            if (customersChunk.isEmpty()) break;

            DistributionsResultDTO chunkResult = individualDistributionOperation.distribute(
                    purchaseOrders, customersChunk, buyNeedsByCustomer, masterAccount, fixedTotalPerTicker);

            deliveryRepository.saveAll(chunkResult.deliveries());
            custodyRepository.saveAll(chunkResult.modifiedCustodies());

            List<IRDedoDuroEvent> irDedoDuroList = irDedoDuroCalculator.calculate(chunkResult.deliveries(), customersChunk);
            List<DedoDuroOutbox> outboxEntries = irDedoDuroOutboxService.createOutboxEntries(
                    irDedoDuroList.stream()
                            .map(IRDedoDuroEvent::toString)
                            .toList());

            dedoDuroOutboxRepository.saveAll(outboxEntries);

            totalCustomersProcessed += customersChunk.size();
            totalDeliveries += chunkResult.deliveries().size();
            totalOutboxEntries += outboxEntries.size();

            if (purchaseOrdersPerAssets.isEmpty())
                purchaseOrdersPerAssets.addAll(chunkResult.purchaseOrdersPerAssets());

            if (customersChunk.size() < BATCH_SIZE) break;

            lastId = customersChunk.keySet().stream()
                    .max(Long::compareTo)
                    .orElse(lastId);
        }

        custodyRepository.saveAll(masterAccount.getCustodies());

        long elapsedTime = System.currentTimeMillis() - startTime;
        log.info("Individual distribution completed in {}ms ({} seconds)", elapsedTime, elapsedTime / 1000.0);

        return new PurchaseSummaryDTO(
                purchaseOrdersPerAssets,
                getResidualsFromMaster(masterAccount),
                totalCustomersProcessed,
                totalDeliveries,
                totalOutboxEntries);
    }

    public void updateMasterWithPurchaseQuantity(BrokerageAccount masterAccount, List<PurchaseOrder> purchaseOrders) {
        log.info("Start updating residuals from master.");

        List<Custody> masterCustodies = new ArrayList<>(masterAccount.getCustodies());
        masterAccount.setCustodies(masterCustodies);

        Map<String, Custody> custodiesByTicker = new HashMap<>();
        masterCustodies.forEach(custody -> custodiesByTicker.put(custody.getTicker(), custody));

        for (PurchaseOrder purchaseOrder : purchaseOrders) {
            if (purchaseOrder.getQuantity() <= 0) continue;

            Custody custody = custodiesByTicker.computeIfAbsent(purchaseOrder.getTicker(), ticker -> {
                Custody newCustody = Custody.create(masterAccount, ticker);
                masterCustodies.add(newCustody);
                return newCustody;
            });

            int oldQuantity = custody.getQuantity();
            custody.addPurchaseQuantity(purchaseOrder.getQuantity(), purchaseOrder.getUnitPrice());
            log.info("Master Custody: {} from {} to {}", custody.getTicker(), oldQuantity, custody.getQuantity());
        }

        log.info("Finished updating residuals from master.");

    }

    private List<ResidualsFromMaster> getResidualsFromMaster(BrokerageAccount masterAccount) {
        return masterAccount.getCustodies().stream()
                .map(custody -> new ResidualsFromMaster(custody.getTicker(), custody.getQuantity()))
                .toList();
    }
}
