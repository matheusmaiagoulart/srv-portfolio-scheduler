package com.matheus.srv_portfolio_scheduler.domain.exceptions;

public class QuoteNotFoundException extends BusinessException {

    public static final String CODE = "QUOTE_NOT_FOUND";
    public static final String MESSAGE = "Quote not found for ticker: ";

    public QuoteNotFoundException(String ticker) {
        super(CODE, String.format(MESSAGE, ticker));
    }
}
