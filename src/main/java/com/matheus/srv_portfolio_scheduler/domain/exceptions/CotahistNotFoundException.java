package com.matheus.srv_portfolio_scheduler.domain.exceptions;

public class CotahistNotFoundException extends BusinessException {

    private static final String CODE = "COTAHIST_NOT_FOUND";
    private static final String MESSAGE = "Cotahist not found or empty.";

    public CotahistNotFoundException() {
        super(CODE, MESSAGE);
    }
}
