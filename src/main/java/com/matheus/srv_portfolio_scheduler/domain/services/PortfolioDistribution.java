package com.matheus.srv_portfolio_scheduler.domain.services;

import com.matheus.srv_portfolio_scheduler.application.command.ExecutePortfolioPurchase.ResidualsFromMaster;
import com.matheus.srv_portfolio_scheduler.domain.entities.BrokerageAccount;
import com.matheus.srv_portfolio_scheduler.domain.entities.Custody;
import com.matheus.srv_portfolio_scheduler.domain.entities.Delivery;
import com.matheus.srv_portfolio_scheduler.domain.entities.PurchaseOrder;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.*;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class PortfolioDistribution {

    public DistributionsResultDTO distribute(List<PurchaseOrder> purchaseOrders, PurchaseRoundDataDTO purchaseRoundData, BrokerageAccount masterAccount) {


        DistributionContext context = buildContext(purchaseOrders, masterAccount);
        log.info("Starting distribution process for purchase orders: {}, TotalAmount: {}, Residuals from Master: {}",
                purchaseOrders, purchaseRoundData.totalPurchaseAmount(), context.masterCustodies);

        if (purchaseRoundData.totalPurchaseAmount().getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            log.info("No purchase amount to distribute. Returning empty distributions.");
            return new DistributionsResultDTO(
                    createResponseForAssetsPurchased(purchaseOrders, context.masterCustodies),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>());
        }

        DistributionOutput distributionOutput = distributeToCustomers(purchaseRoundData, context);

        List<ResidualsFromMaster> residualsFromMaster = updateResidualsFromMaster(
                distributionOutput.distributedPerTicker,
                context.totalPerTicker, masterAccount);

        return new DistributionsResultDTO(createResponseForAssetsPurchased(purchaseOrders, context.masterCustodies),
                distributionOutput.distributions,
                residualsFromMaster,
                distributionOutput.deliveries,
                distributionOutput.modifiedCustodies);
    }

    private DistributionContext buildContext(List<PurchaseOrder> purchaseOrders, BrokerageAccount masterAccount) {
        Map<String, Custody> masterCustodies = getMasterCustodies(masterAccount);
        Map<String, Integer> distributedPerTicker = buildDistributedPerTicker(masterAccount);
        Map<String, TickerData> purchasedPerTicker = buildPurchasedPerTicker(purchaseOrders);
        Map<String, TickerData> totalPerTicker = buildTotalQuantityPerTickerToDistribute(purchasedPerTicker, masterAccount);

        return new DistributionContext(masterCustodies, distributedPerTicker, purchasedPerTicker, totalPerTicker);
    }

    private DistributionOutput distributeToCustomers(PurchaseRoundDataDTO purchaseRoundData, DistributionContext context) {

        List<Custody> modifiedCustodies = new ArrayList<>();
        List<Delivery> responseDeliveries = new ArrayList<>();
        List<Distributions> responseDistributions = new ArrayList<>();
        Map<String, Integer> distributedPerTicker = context.distributedPerTicker();

        log.info("Initializing round of distribution for customers.");

        purchaseRoundData.custodiesPerCustomer().forEach((customerId, customer) -> {

            List<Custody> customerCustodies = customer.customerCustodies().getCustodies();
            List<DistributionsPerAsset> distributionsToCustomer = new ArrayList<>();

            BigDecimal proportion = customer.thirdPartyBalance().getAmount()
                    .divide(purchaseRoundData.totalPurchaseAmount().getAmount(), 2, RoundingMode.HALF_DOWN);

            customerCustodies.forEach(custody -> {
                Money tickerPrice = context.totalPerTicker().get(custody.getTicker()).assetPrice();
                int tickerQuantity = context.totalPerTicker().get(custody.getTicker()).totalQuantity();

                int quantityToDistribute = BigDecimal.valueOf(tickerQuantity)
                        .multiply(proportion)
                        .setScale(0, RoundingMode.DOWN)
                        .intValue();

                if (quantityToDistribute <= 0) return;

                custody.addPurchaseQuantity(quantityToDistribute, tickerPrice);
                modifiedCustodies.add(custody);

                distributionsToCustomer.add(new DistributionsPerAsset(custody.getTicker(), quantityToDistribute));
                distributedPerTicker.put(custody.getTicker(), distributedPerTicker.get(custody.getTicker()) + quantityToDistribute);

                responseDeliveries.add(
                        Delivery.createDelivery(
                                context.totalPerTicker().get(custody.getTicker()).purchaseId(),
                                customer.brokerageAccountId(),
                                custody.getTicker(),
                                quantityToDistribute,
                                tickerPrice));
            });

            log.info("Finished round of distribution for customer: {}, Total distributed: {}",
                    customer.customerId(), distributionsToCustomer);

            responseDistributions.add(new Distributions(
                    customer.customerId(),
                    customer.fullName(),
                    customer.thirdPartyBalance(),
                    distributionsToCustomer));
        });

        log.info("Round of distribution process completed. Total deliveries: {}, Total client distributions: {}",
                responseDeliveries.size(), responseDistributions.stream().filter(d -> !d.distributionsPerAssets().isEmpty()).count());

        return new DistributionOutput(responseDeliveries, responseDistributions, distributedPerTicker, modifiedCustodies);
    }

    private List<ResidualsFromMaster> updateResidualsFromMaster(Map<String, Integer> distributedPerTicker, Map<String, TickerData> totalQuantityPerTickerToDistribute, BrokerageAccount masterAccount) {
        log.info("Start updating residuals from master.");

        List<ResidualsFromMaster> result = masterAccount.getCustodies().stream()
                .map(custody -> {
                    int totalQuantity = totalQuantityPerTickerToDistribute.get(custody.getTicker()).totalQuantity();
                    int distributedQuantity = distributedPerTicker.get(custody.getTicker());
                    int residualQuantity = totalQuantity - distributedQuantity;

                    if (residualQuantity <= 0) {
                        log.info("Residual quantity for ticker {} is zero or negative ({}). No update needed.", custody.getTicker(), residualQuantity);
                        return null;
                    }

                    log.info("Master Custody: {} from {} to {}", custody.getTicker(), custody.getQuantity(), residualQuantity);
                    custody.updateResidualQuantity(residualQuantity, totalQuantityPerTickerToDistribute.get(custody.getTicker()).assetPrice(), masterAccount.getAccountType());
                    return new ResidualsFromMaster(custody.getTicker(), residualQuantity);
                })
                .filter(Objects::nonNull)
                .toList();

        log.info("Finished updating residuals from master.");
        return result;
    }

    private Map<String, Custody> getMasterCustodies(BrokerageAccount masterAccount) {
        return masterAccount.getCustodies().stream().collect(Collectors.toMap(
                Custody::getTicker,
                custody -> custody));
    }

    // BUILD METHODS
    private Map<String, TickerData> buildPurchasedPerTicker(List<PurchaseOrder> purchaseOrders) {
        return purchaseOrders.stream().collect(Collectors.toMap(
                PurchaseOrder::getTicker, // KEY
                order -> new TickerData(order.getId(), order.getQuantity(), order.getUnitPrice()), // VALUE

                // IF KEY EXISTS, THIS FUNCTION WILL BE EXECUTED
                (existingTicker, newTicker) -> new TickerData(
                        existingTicker.purchaseId(),
                        existingTicker.totalQuantity() + newTicker.totalQuantity(),
                        existingTicker.assetPrice())
        ));
    }

    private Map<String, TickerData> buildTotalQuantityPerTickerToDistribute(Map<String, TickerData> purchasedTickers, BrokerageAccount masterAccount) {
        return masterAccount.getCustodies().stream()
                .collect(Collectors.toMap(
                        Custody::getTicker,
                        custody -> {
                            var purchasedTicker = purchasedTickers.get(custody.getTicker());
                            return new TickerData(
                                    purchasedTicker != null ? purchasedTicker.purchaseId() : 0L,
                                    purchasedTicker != null ? custody.getQuantity() + purchasedTicker.totalQuantity() : 0,
                                    purchasedTicker != null ? purchasedTicker.assetPrice() : custody.getAveragePrice());
                        })
                );
    }

    private Map<String, Integer> buildDistributedPerTicker(BrokerageAccount masterAccount) {
        return masterAccount.getCustodies().stream()
                .collect(Collectors.toMap(
                        Custody::getTicker,
                        custody -> 0));
    }

    // RESPONSE METHODS
    private List<PurchaseOrdersPerAsset> createResponseForAssetsPurchased(List<PurchaseOrder> purchasedAssets, Map<String, Custody> residualsFromMaster) {
        return purchasedAssets.stream()
                .map(assetOrder -> new PurchaseOrdersPerAsset(
                        assetOrder.getTicker(),
                        assetOrder.getQuantity(),
                        new PurchaseOrdersPerAssetDetails(
                                assetOrder.getMarketType().toString(),
                                assetOrder.getTicker(),
                                assetOrder.getQuantity()
                        ),
                        assetOrder.getUnitPrice())
                ).collect(Collectors.toList());
    }

    // CONTEXT DATA NECESSARY TO PROCESS DISTRIBUTION
    private record DistributionContext(
            Map<String, Custody> masterCustodies,
            Map<String, Integer> distributedPerTicker,
            Map<String, TickerData> purchasedPerTicker,
            Map<String, TickerData> totalPerTicker
    ) {
    }

    private record DistributionOutput(
            List<Delivery> deliveries,
            List<Distributions> distributions,
            Map<String, Integer> distributedPerTicker,
            List<Custody> modifiedCustodies
    ) {
    }
}
