package com.matheus.srv_portfolio_scheduler.application.ports.output.queries;

import com.matheus.srv_portfolio_scheduler.application.queries.GetAllRecommendedPortfolios.GetAllRecommendedPortfoliosResponse;

import java.util.List;

public interface RecommendedPortfolioQueryRepositoryPort {
    GetAllRecommendedPortfoliosResponse getAllRecommendedPortfolios();
}
