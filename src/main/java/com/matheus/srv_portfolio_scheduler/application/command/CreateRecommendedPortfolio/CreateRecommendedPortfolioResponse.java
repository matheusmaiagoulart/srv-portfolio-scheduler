package com.matheus.srv_portfolio_scheduler.application.command.CreateRecommendedPortfolio;

import com.matheus.srv_portfolio_scheduler.domain.entities.RecommendedPortfolio;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.List;

@Builder(access = AccessLevel.PRIVATE)
public record CreateRecommendedPortfolioResponse(
        long id,
        String name,
        boolean active,
        OffsetDateTime createdAt,
        List<PortfolioItemResponse> portfolioItems,
        boolean rebalanceTriggered,
        String message) {

    public static CreateRecommendedPortfolioResponse successfullyCreated(RecommendedPortfolio portfolio, boolean rebalanceTriggered) {
        return CreateRecommendedPortfolioResponse.builder()
                .id(portfolio.getId())
                .name(portfolio.getName())
                .active(portfolio.isActive())
                .createdAt(portfolio.getCreatedAt())
                .portfolioItems(portfolio.getPortfolioItems().stream().map(PortfolioItemResponse::fromDomain).toList())
                .rebalanceTriggered(rebalanceTriggered)
                .message("Recommended portfolio created successfully.")
                .build();
    }
}
