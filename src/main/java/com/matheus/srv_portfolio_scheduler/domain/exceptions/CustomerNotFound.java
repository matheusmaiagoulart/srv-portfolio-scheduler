package com.matheus.srv_portfolio_scheduler.domain.exceptions;

public class CustomerNotFound extends BusinessException {

    private static final String CODE = "CUSTOMER_NOT_FOUND";
    private static final String MESSAGE = "Customer not found for id: ";

    public CustomerNotFound(Long customerId) {
        super(CODE, String.format(MESSAGE + customerId));
    }
}
