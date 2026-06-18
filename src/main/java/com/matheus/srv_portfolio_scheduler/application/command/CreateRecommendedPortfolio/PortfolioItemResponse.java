package com.matheus.srv_portfolio_scheduler.application.command.CreateRecommendedPortfolio;

import com.matheus.srv_portfolio_scheduler.domain.entities.PortfolioItem;

public record PortfolioItemResponse(
        String ticker,
        int percentage) {

    static PortfolioItemResponse fromDomain(PortfolioItem portfolioItem) {
        return new PortfolioItemResponse(portfolioItem.getTicker(), portfolioItem.getPercentage());
    }
}
