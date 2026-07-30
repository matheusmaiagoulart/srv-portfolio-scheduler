package com.matheus.srv_portfolio_scheduler.domain.exceptions;

public class CustomerAlreadyInactiveException extends BusinessException {

    private static final String CODE = "CUSTOMER_ALREADY_INACTIVE";
    private static final String MESSAGE = "Customer is already inactive for id: ";

    public CustomerAlreadyInactiveException(Long customerId) {
        super(CODE, MESSAGE + customerId);
    }
}

