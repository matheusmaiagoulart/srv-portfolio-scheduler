package com.matheus.srv_portfolio_scheduler.application.service;

import com.matheus.srv_portfolio_scheduler.application.command.ExecutePortfolioPurchase.ResidualsFromMaster;
import com.matheus.srv_portfolio_scheduler.application.ports.output.*;
import com.matheus.srv_portfolio_scheduler.domain.entities.BrokerageAccount;
import com.matheus.srv_portfolio_scheduler.domain.entities.Custody;
import com.matheus.srv_portfolio_scheduler.domain.entities.Delivery;
import com.matheus.srv_portfolio_scheduler.domain.entities.PurchaseOrder;
import com.matheus.srv_portfolio_scheduler.domain.events.IRDedoDuroEvent;
import com.matheus.srv_portfolio_scheduler.domain.services.IRDedoDuroCalculator;
import com.matheus.srv_portfolio_scheduler.domain.services.PortfolioDistribution;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.*;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProcessDistributionInBatch {

    private final DedoDuroOutboxRepositoryPort dedoDuroOutboxRepository;
    private final DistributionPersistenceService distributionPersistence;
    private final IRDedoDuroOutboxService irDedoDuroOutboxService;
    private final IRDedoDuroCalculator irDedoDuroCalculator;
    private final CustomerRepositoryPort customerRepository;
    private final DeliveryRepositoryPort deliveryRepository;
    private final CustodyRepositoryPort custodyRepository;
    private final BrokerageAccountRepositoryPort brokerageAccountRepository;
    private final PortfolioDistribution portfolioDistribution;

    @Value("${app.customers_batch_size}")
    private int BATCH_SIZE;

    @Transactional
    public DistributionsResultDTO processInBatch(List<PurchaseOrder> purchaseOrders, Money thirdValue, BrokerageAccount masterAccount) {

        long lastId = 0;

        // Initialize the response lists
        List<Delivery> responseDeliveries = new ArrayList<>();
        List<Distributions> responseDistributions = new ArrayList<>();
        List<ResidualsFromMaster> responseResidualsFromMaster = new ArrayList<>();
        List<PurchaseOrdersPerAsset> responsePurchaseOrdersPerAssets = new ArrayList<>();
        List<Custody> modifiedCustodies = new ArrayList<>();

        while (true) {

            Map<Long, CustodyPurchaseDataDTO> customersChunk = customerRepository.getChunkOfCustomers(lastId, BATCH_SIZE);
            if (customersChunk.isEmpty()) break;

            PurchaseRoundDataDTO purchaseRoundData = new PurchaseRoundDataDTO(thirdValue, customersChunk);

            DistributionsResultDTO distributionsResult = portfolioDistribution.distribute(purchaseOrders, purchaseRoundData, masterAccount);

            responseDistributions.addAll(distributionsResult.distributions());
            responsePurchaseOrdersPerAssets.addAll(distributionsResult.purchaseOrdersPerAssets());
            responseResidualsFromMaster.addAll(distributionsResult.residualsFromMaster());
            responseDeliveries.addAll(distributionsResult.deliveries());
            modifiedCustodies.addAll(distributionsResult.modifiedCustodies());

            deliveryRepository.saveAll(responseDeliveries);
            custodyRepository.saveAll(modifiedCustodies);

            List<IRDedoDuroEvent> irDedoDuroList = irDedoDuroCalculator.calculate(responseDeliveries, customersChunk);
            dedoDuroOutboxRepository.saveAll(irDedoDuroOutboxService.createOutboxEntries(irDedoDuroList.stream().map(IRDedoDuroEvent::toString).toList()));


            if (customersChunk.size() < BATCH_SIZE) break;

            lastId = customersChunk.keySet().stream().max(Long::compareTo).orElse(lastId);
        }

        brokerageAccountRepository.save(masterAccount);

        return new DistributionsResultDTO(responsePurchaseOrdersPerAssets, responseDistributions, responseResidualsFromMaster, responseDeliveries, modifiedCustodies);
    }
}
