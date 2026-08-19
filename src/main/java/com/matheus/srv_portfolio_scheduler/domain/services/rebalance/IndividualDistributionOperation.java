package com.matheus.srv_portfolio_scheduler.domain.services.rebalance;

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

/**
 * Distributes purchased shares to each customer based on their EXACT
 * individual buy need (calculated during the rebalance sell round),
 * instead of a proportional share of a shared pool.
 * <p>
 * If a customer needs a ticker they never held before (brand new asset),
 * a new zeroed Custody is created for them on the fly.
 */
@Slf4j
public class IndividualDistributionOperation {

    public DistributionsResultDTO distribute(
            List<PurchaseOrder> purchaseOrders,
            Map<Long, CustodyPurchaseDataDTO> customersChunk,
            Map<Long, CustomerBuyNeedDTO> buyNeedsByCustomer,
            BrokerageAccount masterAccount,
            Map<String, TickerData> fixedTotalPerTicker) {

        Map<String, Custody> masterCustodies = masterAccount.getCustodies().stream()
                .collect(Collectors.toMap(Custody::getTicker, custody -> custody));

        List<Custody> modifiedCustodies = new ArrayList<>();
        List<Delivery> deliveries = new ArrayList<>();
        List<Distributions> distributions = new ArrayList<>();

        for (CustodyPurchaseDataDTO customer : customersChunk.values()) {
            CustomerBuyNeedDTO buyNeed = buyNeedsByCustomer.get(customer.customerId());
            if (buyNeed == null || buyNeed.neededAmountPerTicker().isEmpty()) continue;

            BrokerageAccount customerAccount = customer.getCustomerCustodies();

            // Garante que a lista de custódias seja mutável, independente
            // de como foi construída pelo mapper/repositório.
            List<Custody> customerCustodies = new ArrayList<>(customerAccount.getCustodies());
            customerAccount.setCustodies(customerCustodies);

            List<DistributionsPerAsset> distributionsToCustomer = new ArrayList<>();

            for (Map.Entry<String, Money> need : buyNeed.neededAmountPerTicker().entrySet()) {
                String ticker = need.getKey();
                Money neededAmount = need.getValue();

                TickerData tickerData = fixedTotalPerTicker.get(ticker);
                if (tickerData == null || tickerData.assetPrice().getAmount().compareTo(BigDecimal.ZERO) <= 0) continue;

                int quantityToDistribute = neededAmount.getAmount()
                        .divide(tickerData.assetPrice().getAmount(), 0, RoundingMode.DOWN)
                        .intValue();

                Custody masterCustody = masterCustodies.get(ticker);
                int availableQuantity = masterCustody != null ? masterCustody.getQuantity() : 0;
                quantityToDistribute = Math.min(quantityToDistribute, availableQuantity);

                if (quantityToDistribute <= 0) continue;

                Custody custody = findOrCreateCustody(customerCustodies, customerAccount, ticker);
                custody.addPurchaseQuantity(quantityToDistribute, tickerData.assetPrice());
                modifiedCustodies.add(custody);

                masterCustody.subtractQuantity(quantityToDistribute);

                distributionsToCustomer.add(new DistributionsPerAsset(ticker, quantityToDistribute));

                deliveries.add(Delivery.createDelivery(
                        tickerData.purchaseId(),
                        customer.brokerageAccountId(),
                        ticker,
                        quantityToDistribute,
                        tickerData.assetPrice()));
            }

            if (!distributionsToCustomer.isEmpty()) {
                distributions.add(new Distributions(
                        customer.customerId(),
                        customer.fullName(),
                        customer.thirdPartyBalance(),
                        distributionsToCustomer));
            }
        }

        log.info("Individual distribution completed. Total deliveries: {}, Total client distributions: {}",
                deliveries.size(), distributions.size());

        List<PurchaseOrdersPerAsset> purchaseOrdersPerAssets = purchaseOrders.stream()
                .map(order -> new PurchaseOrdersPerAsset(
                        order.getTicker(),
                        order.getQuantity(),
                        new PurchaseOrdersPerAssetDetails(order.getMarketType().toString(), order.getTicker(), order.getQuantity()),
                        order.getUnitPrice()))
                .collect(Collectors.toList());

        return new DistributionsResultDTO(purchaseOrdersPerAssets, distributions, deliveries, modifiedCustodies);
    }

    private Custody findOrCreateCustody(List<Custody> customerCustodies, BrokerageAccount customerAccount, String ticker) {
        return customerCustodies.stream()
                .filter(c -> c.getTicker().equals(ticker))
                .findFirst()
                .orElseGet(() -> {
                    Custody newCustody = Custody.create(customerAccount, ticker);
                    customerCustodies.add(newCustody);
                    return newCustody;
                });
    }
}

