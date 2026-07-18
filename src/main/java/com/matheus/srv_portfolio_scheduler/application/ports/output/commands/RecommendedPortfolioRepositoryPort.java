package com.matheus.srv_portfolio_scheduler.application.ports.output.commands;

import com.matheus.srv_portfolio_scheduler.domain.entities.RecommendedPortfolio;

import java.util.Optional;

public interface RecommendedPortfolioRepositoryPort {

    void save(RecommendedPortfolio recommendedPortfolio);
    Optional<RecommendedPortfolio> getActiveRecommendedPortfolio();
}
