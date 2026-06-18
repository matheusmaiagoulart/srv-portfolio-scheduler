package com.matheus.srv_portfolio_scheduler.adapters.exception.handler;

public record ExceptionResponse(
        int HTTP_STATUS,
        String ERROR_CODE,
        String MESSAGE
) {
}
