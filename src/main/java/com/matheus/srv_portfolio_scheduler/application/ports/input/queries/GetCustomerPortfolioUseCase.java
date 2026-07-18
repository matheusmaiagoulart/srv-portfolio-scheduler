package com.matheus.srv_portfolio_scheduler.application.ports.input.queries;

import com.matheus.srv_portfolio_scheduler.application.queries.GetCustomerPortfolio.GetCustomerPortfolioQuery;
import com.matheus.srv_portfolio_scheduler.application.queries.GetCustomerPortfolio.GetCustomerPortfolioResponse;

public interface GetCustomerPortfolioUseCase {
    GetCustomerPortfolioResponse handler(GetCustomerPortfolioQuery query);
}
