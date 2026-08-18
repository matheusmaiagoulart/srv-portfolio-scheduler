package com.matheus.srv_portfolio_scheduler.domain.services.rebalance;

import com.matheus.srv_portfolio_scheduler.domain.services.dto.PortfolioComparisonDTO;
import com.matheus.srv_portfolio_scheduler.domain.entities.Custody;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.CustomerBuyNeedDTO;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * Calculates, for a single customer, how much money needs to be spent on
 * each ticker of the new recommended portfolio to reach its target
 * composition, based on the customer's original portfolio value and the
 * current price of each ticker.
 * <p>
 * Only two situations generate a buy need:
 * - an altered ticker whose percentage INCREASED (target > current)
 * - a brand new ticker added to the portfolio (current is effectively 0)
 */
public class CalculateBuyNeedOperation {

    public CustomerBuyNeedDTO execute(
            Long customerId,
            Map<String, Custody> custodies,
            PortfolioComparisonDTO comparisonDTO,
            Map<String, Money> pricesByTicker,
            Money originalPortfolioValue,
            Money availableAmount) {

        Map<String, Money> neededAmountPerTicker = new HashMap<>();

        for (PortfolioComparisonDTO.AlteredItem item : comparisonDTO.altered()) {
            if (!item.isIncrease()) continue;

            Money price = pricesByTicker.get(item.ticker());
            if (price == null) continue;

            Money targetValue = originalPortfolioValue.multiply(
                    item.newPercentage().divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
            Money currentValue = getCurrentCustodyValue(custodies.get(item.ticker()), price);

            addNeedIfPositive(neededAmountPerTicker, item.ticker(), targetValue, currentValue);
        }

        for (PortfolioComparisonDTO.NewItem item : comparisonDTO.added()) {
            Money price = pricesByTicker.get(item.ticker());
            if (price == null) continue;

            Money targetValue = originalPortfolioValue.multiply(
                    item.percentage().divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
            Money currentValue = getCurrentCustodyValue(custodies.get(item.ticker()), price);

            addNeedIfPositive(neededAmountPerTicker, item.ticker(), targetValue, currentValue);
        }

        return new CustomerBuyNeedDTO(
                customerId,
                limitToAvailableAmount(neededAmountPerTicker, availableAmount));
    }

    private void addNeedIfPositive(Map<String, Money> neededAmountPerTicker, String ticker, Money targetValue, Money currentValue) {
        BigDecimal need = targetValue.getAmount().subtract(currentValue.getAmount());
        if (need.compareTo(BigDecimal.ZERO) > 0) {
            neededAmountPerTicker.put(ticker, Money.create(need));
        }
    }

    private Money getCurrentCustodyValue(Custody custody, Money price) {
        if (custody == null) return Money.create(BigDecimal.ZERO);
        return Money.create(price.getAmount().multiply(BigDecimal.valueOf(custody.getQuantity())));
    }

    private Map<String, Money> limitToAvailableAmount(
            Map<String, Money> neededAmountPerTicker,
            Money availableAmount) {

        BigDecimal totalNeed = neededAmountPerTicker.values().stream()
                .map(Money::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalNeed.compareTo(BigDecimal.ZERO) == 0) return neededAmountPerTicker;
        if (availableAmount.getAmount().compareTo(BigDecimal.ZERO) <= 0) return Map.of();
        if (totalNeed.compareTo(availableAmount.getAmount()) <= 0) return neededAmountPerTicker;

        BigDecimal factor = availableAmount.getAmount()
                .divide(totalNeed, 12, RoundingMode.DOWN);

        Map<String, Money> adjustedNeeds = new HashMap<>();
        neededAmountPerTicker.forEach((ticker, amount) -> {
            BigDecimal adjustedAmount = amount.getAmount()
                    .multiply(factor)
                    .setScale(2, RoundingMode.DOWN);

            if (adjustedAmount.compareTo(BigDecimal.ZERO) > 0) {
                adjustedNeeds.put(ticker, Money.create(adjustedAmount));
            }
        });

        return adjustedNeeds;
    }
}





