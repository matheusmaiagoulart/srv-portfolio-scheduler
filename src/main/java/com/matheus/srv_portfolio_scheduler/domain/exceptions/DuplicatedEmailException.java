package com.matheus.srv_portfolio_scheduler.domain.exceptions;

public class DuplicatedEmailException extends BusinessException {

    private static final String errorCode = "DUPLICATED_EMAIL";
    private static final String errorMessage = "EMAIL already exists: ";

    public DuplicatedEmailException(String email) {
        super(errorCode, errorMessage.concat(email));
    }
}
