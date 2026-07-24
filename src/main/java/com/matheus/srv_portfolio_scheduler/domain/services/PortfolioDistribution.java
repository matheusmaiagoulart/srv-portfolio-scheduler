package com.matheus.srv_portfolio_scheduler.domain.services;

import com.matheus.srv_portfolio_scheduler.application.dto.DistributionContext;
import com.matheus.srv_portfolio_scheduler.application.dto.DistributionOutput;
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
import java.util.stream.Collectors;

@Slf4j
public class PortfolioDistribution {

    public DistributionsResultDTO distribute(List<PurchaseOrder> purchaseOrders, PurchaseRoundDataDTO purchaseRoundData, BrokerageAccount masterAccount, Map<String, TickerData> fixedTotalPerTicker) {

        DistributionContext context = buildContext(purchaseOrders, masterAccount, fixedTotalPerTicker);

        log.info("Starting distribution process for purchase orders: {}, TotalAmount: {}, Master custodies: {}",
                purchaseOrders.stream().map(p -> p.getTicker() + "=" + p.getQuantity()).toList(),
                purchaseRoundData.totalPurchaseAmount().getAmount(),
                context.masterCustodies().entrySet().stream()
                        .map(e -> e.getKey() + "=" + e.getValue().getQuantity())
                        .toList());

        if (purchaseRoundData.totalPurchaseAmount().getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            log.info("No purchase amount to distribute. Returning empty distributions.");
            return new DistributionsResultDTO(
                    createResponseForAssetsPurchased(purchaseOrders),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>());
        }

        DistributionOutput distributionOutput = distributeToCustomers(purchaseRoundData, context);

        return new DistributionsResultDTO(
                createResponseForAssetsPurchased(purchaseOrders),
                distributionOutput.distributions(),
                distributionOutput.deliveries(),
                distributionOutput.modifiedCustodies());
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

            customerCustodies.forEach(custody -> {
                Money tickerPrice = context.totalPerTicker().get(custody.getTicker()).assetPrice();
                int tickerQuantity = context.totalPerTicker().get(custody.getTicker()).totalQuantity();

                int quantityToDistribute = customer.thirdPartyBalance().getAmount()
                        .multiply(BigDecimal.valueOf(tickerQuantity))
                        .divide(purchaseRoundData.totalPurchaseAmount().getAmount(), 0, RoundingMode.DOWN)
                        .intValue();

                if (quantityToDistribute <= 0) return;

                custody.addPurchaseQuantity(quantityToDistribute, tickerPrice);
                modifiedCustodies.add(custody);

                context.masterCustodies().get(custody.getTicker()).subtractQuantity(quantityToDistribute);

                distributionsToCustomer.add(new DistributionsPerAsset(custody.getTicker(), quantityToDistribute));
                distributedPerTicker.put(custody.getTicker(), distributedPerTicker.get(custody.getTicker()) + quantityToDistribute);

                log.info("Creating delivery with purchaseOrderId: {}, ticker: {}",
                        context.totalPerTicker().get(custody.getTicker()).purchaseId(),
                        custody.getTicker());

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

    // BUILD METHODS

    public Map<String, TickerData> buildInitialTotalPerTicker(List<PurchaseOrder> purchaseOrders, BrokerageAccount masterAccount) {
        Map<String, TickerData> purchasedPerTicker = buildPurchasedPerTicker(purchaseOrders);
        return buildTotalQuantityPerTickerToDistribute(purchasedPerTicker, masterAccount);
    }

    private DistributionContext buildContext(List<PurchaseOrder> purchaseOrders, BrokerageAccount masterAccount, Map<String, TickerData> fixedTotalPerTicker) {
        Map<String, Custody> masterCustodies = getMasterCustodies(masterAccount);
        Map<String, Integer> distributedPerTicker = buildDistributedPerTicker(masterAccount);
        Map<String, TickerData> purchasedPerTicker = buildPurchasedPerTicker(purchaseOrders);

        return new DistributionContext(masterCustodies, distributedPerTicker, purchasedPerTicker, fixedTotalPerTicker);
    }

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
                                    custody.getQuantity(),
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

    private Map<String, Custody> getMasterCustodies(BrokerageAccount masterAccount) {
        return masterAccount.getCustodies().stream().collect(Collectors.toMap(
                Custody::getTicker,
                custody -> custody));
    }

    private List<PurchaseOrdersPerAsset> createResponseForAssetsPurchased(List<PurchaseOrder> purchasedAssets) {
        return purchasedAssets.stream()
                .map(assetOrder -> new PurchaseOrdersPerAsset(
                        assetOrder.getTicker(),
                        assetOrder.getQuantity(),
                        new PurchaseOrdersPerAssetDetails(
                                assetOrder.getMarketType().toString(),
                                assetOrder.getTicker(),
                                assetOrder.getQuantity()),
                        assetOrder.getUnitPrice())
                ).collect(Collectors.toList());
    }
}
