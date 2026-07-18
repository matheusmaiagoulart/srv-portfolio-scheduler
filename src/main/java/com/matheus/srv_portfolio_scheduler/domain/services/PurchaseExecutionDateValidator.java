package com.matheus.srv_portfolio_scheduler.domain.services;

import com.matheus.srv_portfolio_scheduler.domain.exceptions.IllegalPurchaseDayException;
import lombok.extern.slf4j.Slf4j;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Slf4j
public class PurchaseExecutionDateValidator {

    public void validate(LocalDate date) {
        validateExecutionDate(date);
        validateBusinessDay(date);
    }

    private void validateExecutionDate(LocalDate date) {
        if (date.getDayOfMonth() != 5 && date.getDayOfMonth() != 15 && date.getDayOfMonth() != 25) {
            log.error("Invalid execution date: {}. Only the 5th, 15th, and 25th of each month are allowed.", date);
            throw new IllegalPurchaseDayException();
        }
    }

    private void validateBusinessDay(LocalDate date) {
        if (date.getDayOfWeek().equals(DayOfWeek.SATURDAY) || date.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            LocalDate nextDay = date.plusDays(date.getDayOfWeek() == DayOfWeek.SATURDAY ? 2 : 1);
            log.error("Invalid execution date: {}. Next valid business day is {}", date, nextDay);
            throw new IllegalPurchaseDayException(nextDay);

        }
    }
}
