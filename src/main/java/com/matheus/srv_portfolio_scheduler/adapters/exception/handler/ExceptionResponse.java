package com.matheus.srv_portfolio_scheduler.adapters.exception.handler;

public record ExceptionResponse(
        int httpStatus,
        String errorCode,
        String message
) {
}
