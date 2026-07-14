package com.matheus.srv_portfolio_scheduler.domain.exceptions;

import java.time.LocalDate;

public class IllegalPurchaseDayException extends BusinessException {

    public static final String CODE = "ILLEGAL_PURCHASE_DAY";
    public static final String MESSAGE = "The purchase day is not allowed";

    public IllegalPurchaseDayException() {
        super(CODE, MESSAGE);
    }

    public IllegalPurchaseDayException(LocalDate date) {
        super(CODE, String.format(MESSAGE, "Next available date: " +  date));
    }
}
