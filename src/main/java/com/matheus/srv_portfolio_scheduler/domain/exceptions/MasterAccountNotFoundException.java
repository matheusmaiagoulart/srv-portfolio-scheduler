package com.matheus.srv_portfolio_scheduler.domain.exceptions;

public class MasterAccountNotFoundException extends BusinessException {

    private static final String CODE = "MASTER_ACCOUNT_NOT_FOUND";
    private static final String MESSAGE = "Master account not found.";

    public MasterAccountNotFoundException() {
        super(CODE, MESSAGE);
    }
}
