package com.matheus.srv_portfolio_scheduler.src.domain.valueObject;

import com.matheus.srv_portfolio_scheduler.domain.exceptions.BusinessException;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    void deveCriarMoneyComValorPositivo() {
        Money money = Money.create(new BigDecimal("100.00"));

        assertEquals(0, money.getAmount().compareTo(new BigDecimal("100.00")));
    }

    @Test
    void deveCriarMoneyComValorZero() {
        Money money = Money.create(BigDecimal.ZERO);

        assertEquals(0, money.getAmount().compareTo(BigDecimal.ZERO));
    }

    @Test
    void deveArredondarParaDuasCasasDecimais() {
        Money money = Money.create(new BigDecimal("100.456"));

        assertEquals(0, money.getAmount().compareTo(new BigDecimal("100.46")));
    }

    @Test
    void deveArredondarParaBaixoQuandoMenorQue5() {
        Money money = Money.create(new BigDecimal("100.454"));

        assertEquals(0, money.getAmount().compareTo(new BigDecimal("100.45")));
    }

    @Test
    void deveSomarDoisValores() {
        Money m1 = Money.create(new BigDecimal("100.00"));
        Money m2 = Money.create(new BigDecimal("50.00"));

        Money result = m1.add(m2);

        assertEquals(0, result.getAmount().compareTo(new BigDecimal("150.00")));
    }

    @Test
    void deveLancarExcecaoAoSomarValorNulo() {
        Money m1 = Money.create(new BigDecimal("100.00"));

        BusinessException ex = assertThrows(BusinessException.class, () -> m1.add(null));

        assertEquals("INVALID_AMOUNT", ex.getErrorCode());
    }

    @Test
    void deveLancarExcecaoAoSomarValorNegativo() {
        Money m1 = Money.create(new BigDecimal("100.00"));
        Money m2 = Money.create(new BigDecimal("-50.00"));

        BusinessException ex = assertThrows(BusinessException.class, () -> m1.add(m2));

        assertEquals("INVALID_AMOUNT", ex.getErrorCode());
    }

    @Test
    void deveMultiplicarPorFator() {
        Money money = Money.create(new BigDecimal("100.00"));

        Money result = money.multiply(new BigDecimal("3"));

        assertEquals(0, result.getAmount().compareTo(new BigDecimal("300.00")));
    }

    @Test
    void deveMultiplicarPorFatorDecimal() {
        Money money = Money.create(new BigDecimal("100.00"));

        Money result = money.multiply(new BigDecimal("0.30"));

        assertEquals(0, result.getAmount().compareTo(new BigDecimal("30.00")));
    }

    @Test
    void deveMultiplicarPorQuantidade() {
        Money preco = Money.create(new BigDecimal("35.50"));

        Money total = preco.multiply(BigDecimal.valueOf(100));

        assertEquals(0, total.getAmount().compareTo(new BigDecimal("3550.00")));
    }

    @Test
    void deveDividirPorDivisor() {
        Money money = Money.create(new BigDecimal("300.00"));

        Money result = money.divide(new BigDecimal("3"));

        assertEquals(0, result.getAmount().compareTo(new BigDecimal("100.00")));
    }

    @Test
    void deveDividirEArredondarCorretamente() {
        Money money = Money.create(new BigDecimal("100.00"));

        Money result = money.divide(new BigDecimal("3"));

        assertEquals(0, result.getAmount().compareTo(new BigDecimal("33.33")));
    }

    @Test
    void deveDividirValorMensal() {
        Money mensal = Money.create(new BigDecimal("3500.00"));

        Money terco = mensal.divide(new BigDecimal("3"));

        assertEquals(0, terco.getAmount().compareTo(new BigDecimal("1166.67")));
    }

    @Test
    void deveCriarMoneyComValorNegativo() {
        Money money = Money.create(new BigDecimal("-100.00"));

        assertEquals(0, money.getAmount().compareTo(new BigDecimal("-100.00")));
    }

    @Test
    void deveMultiplicarPorZero() {
        Money money = Money.create(new BigDecimal("100.00"));

        Money result = money.multiply(BigDecimal.ZERO);

        assertEquals(0, result.getAmount().compareTo(BigDecimal.ZERO));
    }

    @Test
    void deveCalcularAliquotaDedoDuro() {
        Money valorOperacao = Money.create(new BigDecimal("5600.00"));

        Money dedoDuro = valorOperacao.multiply(new BigDecimal("0.00005"));

        assertEquals(0, dedoDuro.getAmount().compareTo(new BigDecimal("0.28")));
    }

    @Test
    void deveCalcularValorProporcionalPortfolio() {
        Money total = Money.create(new BigDecimal("3500.00"));
        BigDecimal percentual = new BigDecimal("0.30");

        Money proporcional = total.multiply(percentual);

        assertEquals(0, proporcional.getAmount().compareTo(new BigDecimal("1050.00")));
    }
}
