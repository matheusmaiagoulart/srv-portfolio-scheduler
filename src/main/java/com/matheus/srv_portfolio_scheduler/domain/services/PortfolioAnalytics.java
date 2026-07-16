package com.matheus.srv_portfolio_scheduler.domain.services;

import com.matheus.srv_portfolio_scheduler.domain.entities.Custody;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

public class PortfolioAnalytics {

    /**
     * Calculates the total amount invested across all custodies based on average purchase price.
     * <p>
     * Formula: Σ (averagePrice × quantity) for each custody
     * <p>
     * Represents the total capital deployed — how much was actually paid for all positions.
     */
    public static Money calcTotalAmountInvested(List<Custody> custodies) {
        return custodies.stream()
                .map(custody -> Money.create(
                        custody.getAveragePrice().getAmount()
                                .multiply(BigDecimal.valueOf(custody.getQuantity()))))
                .reduce(Money.create(BigDecimal.ZERO), Money::add);
    }

    /**
     * Calculates the current market value of the entire portfolio.
     * <p>
     * Formula: (currentPrice × quantity) for each custody
     * <p>
     * Reflects the portfolio value if all positions were liquidated at current market prices.
     */
    public static Money calcPortfolioTotalValue(List<Custody> custodies, Map<String, Money> currentPrices) {
        return custodies.stream()
                .map(custody -> {
                    BigDecimal currentPrice = currentPrices.getOrDefault(custody.getTicker(), Money.create(BigDecimal.ZERO)).getAmount();
                    return Money.create(currentPrice.multiply(BigDecimal.valueOf(custody.getQuantity())));
                })
                .reduce(Money.create(BigDecimal.ZERO), Money::add);
    }

    /**
     * Calculates the total Profit and Loss (P/L) across all custodies.
     * <p>
     * Formula: ((currentPrice - averagePrice) × quantity) for each custody
     * <p>
     * Aggregates individual P/L values — positive means overall profit, negative means loss.
     */
    public static Money calcPlTotal(List<Custody> custodies, Map<String, Money> currentPrices) {
        return custodies.stream()
                .map(custody -> custody.calcPl(currentPrices.get(custody.getTicker())))
                .reduce(Money.create(BigDecimal.ZERO), Money::add);
    }

    /**
     * Calculates the overall portfolio profitability percentage.
     * <p>
     * Formula: ((portfolioCurrentValue - totalAmountInvested) / totalAmountInvested) × 100
     * <p>
     * Represents the portfolio's total return relative to the capital invested.
     * Example: invested R$11,053.13, current value R$11,433.25 → profitability = 3.44%
     */
    public static BigDecimal calcPortfolioProfitability(Money portfolioCurrentValue, Money totalAmountInvested) {
        if (totalAmountInvested.getAmount().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal profitability = portfolioCurrentValue.getAmount()
                .subtract(totalAmountInvested.getAmount())
                .divide(totalAmountInvested.getAmount(), 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        return profitability.setScale(2, RoundingMode.HALF_UP);
    }
}
