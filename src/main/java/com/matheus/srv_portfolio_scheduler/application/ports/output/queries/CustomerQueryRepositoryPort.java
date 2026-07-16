package com.matheus.srv_portfolio_scheduler.application.ports.output.queries;

import com.matheus.srv_portfolio_scheduler.application.queries.GetCustomerPortfolio.GetCustomerPortfolioResponse;

public interface CustomerQueryRepositoryPort {
    GetCustomerPortfolioResponse getCustomerPortfolio(long customerId);

}
