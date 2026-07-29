package com.matheus.srv_portfolio_scheduler.domain.exceptions;

public class LatestTradingDateNotFoundException extends BusinessException {

    private static final String CODE = "LATEST_TRADING_DATE_NOT_FOUND";
    private static final String MESSAGE = "Latest trading date not found";

    public LatestTradingDateNotFoundException() {
        super(CODE, MESSAGE);
    }
}
