package com.matheus.srv_portfolio_scheduler.application.queries.GetCustomerPortfolio;

import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record GetCustomerPortfolioResponse(
        long customerId,
        String name,
        long brokerageAccountId,
        LocalDateTime consultDate,
        Resume resume,
        Assets assets
) {
    public record Resume(
            Money totalInvestedAmount,
            Money portfolioCurrentValue,
            Money plTotal,
            BigDecimal plPercentage
    ) { }

    public record Assets(List<AssetsDetails> assetsDetailsList) {
    }

    public record AssetsDetails(
            String ticker,
            int quantity,
            Money averagePrice,
            Money currentPrice,
            Money pl,
            BigDecimal plPercentage,
            Money portfolioCompositionPercentage) {
    }

}
