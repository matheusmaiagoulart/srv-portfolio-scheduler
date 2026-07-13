package com.matheus.srv_portfolio_scheduler.domain.exceptions;

public class ActivePortfolioNotFoundException extends BusinessException {

    private static final String CODE = "NONE_ACTIVE_PORTFOLIO";
    private static final String MESSAGE = "No active portfolio found.";

    public ActivePortfolioNotFoundException() {
        super(CODE, MESSAGE);
    }
}
