package com.matheus.srv_portfolio_scheduler.application.queries.GetCustomerPortfolio;

import jakarta.validation.constraints.Min;

public record GetCustomerPortfolioQuery(
        @Min(value = 1, message = "customerId must be greater than 0")
        long customerId) {
}
