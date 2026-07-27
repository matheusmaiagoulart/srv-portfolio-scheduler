package com.matheus.srv_portfolio_scheduler.application.service;

import com.matheus.srv_portfolio_scheduler.application.command.ExecutePortfolioPurchase.ResidualsFromMaster;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.CustodyRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.CustomerRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.DedoDuroOutboxRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.DeliveryRepositoryPort;
import com.matheus.srv_portfolio_scheduler.domain.entities.BrokerageAccount;
import com.matheus.srv_portfolio_scheduler.domain.entities.DedoDuroOutbox;
import com.matheus.srv_portfolio_scheduler.domain.entities.PurchaseOrder;
import com.matheus.srv_portfolio_scheduler.domain.events.IRDedoDuroEvent;
import com.matheus.srv_portfolio_scheduler.domain.services.IRDedoDuroCalculator;
import com.matheus.srv_portfolio_scheduler.domain.services.PortfolioDistribution;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.*;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
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

    @Value("${app.customers_batch_size}")
    private int BATCH_SIZE;

    @Transactional
    public PurchaseSummaryDTO processInBatch(List<PurchaseOrder> purchaseOrders, Money thirdValue, BrokerageAccount masterAccount) {

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

    public void updateMasterWithPurchaseQuantity(BrokerageAccount masterAccount, List<PurchaseOrder> purchaseOrders) {
        log.info("Start updating residuals from master.");

        masterAccount.getCustodies().forEach(custody -> {
            int totalQuantity = purchaseOrders.stream()
                    .filter(p -> p.getTicker().equals(custody.getTicker()))
                    .mapToInt(PurchaseOrder::getQuantity)
                    .sum();

            Money price = purchaseOrders.stream()
                    .filter(p -> p.getTicker().equals(custody.getTicker()))
                    .map(PurchaseOrder::getUnitPrice)
                    .findFirst()
                    .orElse(Money.create(BigDecimal.ZERO));

            if (totalQuantity > 0) {
                int oldQuantity = custody.getQuantity();
                custody.addPurchaseQuantity(totalQuantity, price);
                log.info("Master Custody: {} from {} to {}", custody.getTicker(), oldQuantity, custody.getQuantity());
            }
        });

        log.info("Finished updating residuals from master.");

    }

    private List<ResidualsFromMaster> getResidualsFromMaster(BrokerageAccount masterAccount) {
        return masterAccount.getCustodies().stream()
                .map(custody -> new ResidualsFromMaster(custody.getTicker(), custody.getQuantity()))
                .toList();
    }
}
