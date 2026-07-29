package com.matheus.srv_portfolio_scheduler.application.queries.GetCurrentRecommendedPortfolio;

import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record GetCurrentRecommendedPortfolioResponse(
        long portfolioId,
        String name,
        boolean active,
        OffsetDateTime createdAt,
        List<ItemsResponse> items
) {
    public record ItemsResponse(
            String ticker,
            BigDecimal percentage,
            Money actualPrice
    ) {
    }
}
