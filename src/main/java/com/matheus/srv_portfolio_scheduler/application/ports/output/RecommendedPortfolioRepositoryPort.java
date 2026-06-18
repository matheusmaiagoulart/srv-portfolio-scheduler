package com.matheus.srv_portfolio_scheduler.application.ports.output;

import com.matheus.srv_portfolio_scheduler.domain.entities.RecommendedPortfolio;

import java.util.Optional;

public interface RecommendedPortfolioRepositoryPort {

    void save(RecommendedPortfolio recommendedPortfolio);
    Optional<RecommendedPortfolio> getActiveRecommendedPortfolio();
}
