package com.matheus.srv_portfolio_scheduler.application.queries.GetAllRecommendedPortfolios;

import java.time.OffsetDateTime;
import java.util.List;

public record GetAllRecommendedPortfoliosResponse(List<RecommendedPortfolioDTO> portfolios) {

    public record RecommendedPortfolioDTO(
            long portfolioId,
            String name,
            boolean active,
            OffsetDateTime createdAt,
            OffsetDateTime disabledAt,
            List<ItemsDTO> items
    ) {
    }

    public record ItemsDTO(
            String ticker,
            int percentage
    ) {
    }
}
