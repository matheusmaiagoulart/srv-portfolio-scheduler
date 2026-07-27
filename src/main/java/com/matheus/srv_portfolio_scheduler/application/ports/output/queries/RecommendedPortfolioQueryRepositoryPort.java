package com.matheus.srv_portfolio_scheduler.application.ports.output.queries;

import com.matheus.srv_portfolio_scheduler.application.queries.GetAllRecommendedPortfolios.GetAllRecommendedPortfoliosResponse;

public interface RecommendedPortfolioQueryRepositoryPort {
    GetAllRecommendedPortfoliosResponse getAllRecommendedPortfolios();
}
