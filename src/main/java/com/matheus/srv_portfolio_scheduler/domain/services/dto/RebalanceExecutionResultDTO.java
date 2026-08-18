package com.matheus.srv_portfolio_scheduler.domain.services.dto;

import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Result of the whole rebalance (sell) round: the total amount released
 * across every customer + master account, and the individual buy needs
 * of each processed customer, so the subsequent purchase/distribution
 * step can deliver to each customer exactly what they need instead of
 * a generic proportional share of a shared pool.
 */
public record RebalanceExecutionResultDTO(
        Money totalReleasedAmount,
        List<CustomerBuyNeedDTO> customerBuyNeeds
) {

    public Map<String, Money> aggregatedNeedPerTicker() {
        Map<String, Money> aggregated = new HashMap<>();

        for (CustomerBuyNeedDTO customerNeed : customerBuyNeeds) {
            customerNeed.neededAmountPerTicker().forEach((ticker, amount) ->
                    aggregated.merge(ticker, amount, Money::add));
        }

        return aggregated;
    }

    public Map<Long, CustomerBuyNeedDTO> byCustomerId() {
        return customerBuyNeeds.stream()
                .collect(Collectors.toMap(CustomerBuyNeedDTO::customerId, customerNeed -> customerNeed));
    }
}

