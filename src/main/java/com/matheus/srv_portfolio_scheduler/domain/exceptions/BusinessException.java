package com.matheus.srv_portfolio_scheduler.domain.exceptions;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final String errorCode;
    private final String message;

    public BusinessException(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }
}
