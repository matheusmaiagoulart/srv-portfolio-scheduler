package com.matheus.srv_portfolio_scheduler.src.domain.services;

import com.matheus.srv_portfolio_scheduler.domain.exceptions.IllegalPurchaseDayException;
import com.matheus.srv_portfolio_scheduler.domain.services.PurchaseExecutionDateValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PurchaseExecutionDateValidatorTest {

    private final PurchaseExecutionDateValidator validator = new PurchaseExecutionDateValidator();

    @ParameterizedTest
    @ValueSource(ints = {5, 15, 25})
    void deveAceitarDiasValidos(int dia) {
        LocalDate date = LocalDate.of(2026, 6, dia);
        assertDoesNotThrow(() -> validator.validate(date));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10, 20, 30})
    void deveLancarExcecaoParaDiasInvalidos(int dia) {
        LocalDate date = LocalDate.of(2026, 6, dia);
        assertThrows(IllegalPurchaseDayException.class, () -> validator.validate(date));
    }

    @Test
    void deveLancarExcecaoQuandoDia5CairNoSabado() {
        LocalDate saturday = LocalDate.of(2026, 9, 5);
        
        IllegalPurchaseDayException ex = assertThrows(
                IllegalPurchaseDayException.class,
                () -> validator.validate(saturday));

        assertTrue(ex.getMessage().contains("2026-09-07"));
    }

    @Test
    void deveLancarExcecaoQuandoDia25CairNoDomingo() {
        LocalDate sunday = LocalDate.of(2026, 10, 25);
        
        IllegalPurchaseDayException ex = assertThrows(
                IllegalPurchaseDayException.class,
                () -> validator.validate(sunday));

        assertTrue(ex.getMessage().contains("2026-10-26"));
    }

    @Test
    void deveLancarExcecaoQuandoDia15CairNoSabado() {
        LocalDate saturday = LocalDate.of(2026, 8, 15);
        
        IllegalPurchaseDayException ex = assertThrows(
                IllegalPurchaseDayException.class,
                () -> validator.validate(saturday));

        assertTrue(ex.getMessage().contains("2026-08-17"));
    }
}
