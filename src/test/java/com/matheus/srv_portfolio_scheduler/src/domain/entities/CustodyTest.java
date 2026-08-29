package com.matheus.srv_portfolio_scheduler.src.domain.entities;

import com.matheus.srv_portfolio_scheduler.domain.entities.BrokerageAccount;
import com.matheus.srv_portfolio_scheduler.domain.entities.Custody;
import com.matheus.srv_portfolio_scheduler.domain.enums.BrokerageAccountType;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class CustodyTest {

    @Test
    void deveCriarCustodiaComQuantidadeZero() {
        BrokerageAccount account = BrokerageAccount.reconstruct(1L, null, "CUSTOMER", null, null, new ArrayList<>());

        Custody custody = Custody.create(account, "PETR4");

        assertEquals("PETR4", custody.getTicker());
        assertEquals(0, custody.getQuantity());
        assertEquals(0, custody.getAveragePrice().getAmount().compareTo(BigDecimal.ZERO));
    }

    @Test
    void deveAdicionarQuantidadeDeCompraEAtualizarPrecoMedio() {
        Custody custody = Custody.reconstruct(1L, null, "PETR4", 100,
                Money.create(new BigDecimal("35.00")), null);

        custody.addPurchaseQuantity(50, Money.create(new BigDecimal("38.00")));

        assertEquals(150, custody.getQuantity());
        assertEquals(0, custody.getAveragePrice().getAmount().compareTo(new BigDecimal("36.00")));
    }

    @Test
    void deveCalcularPrecoMedioCorretamente() {
        Custody custody = Custody.reconstruct(1L, null, "VALE3", 0,
                Money.create(BigDecimal.ZERO), null);

        custody.addPurchaseQuantity(10, Money.create(new BigDecimal("62.00")));
        custody.addPurchaseQuantity(5, Money.create(new BigDecimal("65.00")));

        assertEquals(15, custody.getQuantity());
        assertEquals(0, custody.getAveragePrice().getAmount().compareTo(new BigDecimal("63.00")));
    }

    @Test
    void deveSubtrairQuantidade() {
        Custody custody = Custody.reconstruct(1L, null, "PETR4", 100,
                Money.create(new BigDecimal("35.00")), null);

        custody.subtractQuantity(30);

        assertEquals(70, custody.getQuantity());
    }

    @Test
    void deveLancarExcecaoAoSubtrairMaisQueQuantidadeAtual() {
        Custody custody = Custody.reconstruct(1L, null, "PETR4", 50,
                Money.create(new BigDecimal("35.00")), null);

        assertThrows(IllegalArgumentException.class, () -> custody.subtractQuantity(100));
    }

    @Test
    void deveVenderQuantidadeERetornarValorRealizado() {
        Custody custody = Custody.reconstruct(1L, null, "PETR4", 100,
                Money.create(new BigDecimal("35.00")), null);

        Money realised = custody.sell(40, Money.create(new BigDecimal("40.00")));

        assertEquals(60, custody.getQuantity());
        assertEquals(0, realised.getAmount().compareTo(new BigDecimal("1600.00")));
    }

    @Test
    void deveLancarExcecaoAoVenderMaisQueQuantidadeAtual() {
        Custody custody = Custody.reconstruct(1L, null, "PETR4", 50,
                Money.create(new BigDecimal("35.00")), null);

        assertThrows(IllegalArgumentException.class,
                () -> custody.sell(100, Money.create(new BigDecimal("40.00"))));
    }

    @Test
    void deveVenderTodaQuantidade() {
        Custody custody = Custody.reconstruct(1L, null, "PETR4", 100,
                Money.create(new BigDecimal("35.00")), null);

        custody.sellAllQuantity();

        assertEquals(0, custody.getQuantity());
        assertEquals(0, custody.getAveragePrice().getAmount().compareTo(BigDecimal.ZERO));
    }

    @Test
    void deveMigrarTicker() {
        Custody custody = Custody.reconstruct(1L, null, "ITUB3", 100,
                Money.create(new BigDecimal("25.00")), null);

        custody.migrateAsset("ITUB4");

        assertEquals("ITUB4", custody.getTicker());
        assertEquals(100, custody.getQuantity());
    }

    @Test
    void deveCalcularPLComLucro() {
        Custody custody = Custody.reconstruct(1L, null, "PETR4", 68,
                Money.create(new BigDecimal("49.41")), null);

        Money pl = custody.calcPl(Money.create(new BigDecimal("55.00")));

        assertEquals(0, pl.getAmount().compareTo(new BigDecimal("380.12")));
    }

    @Test
    void deveCalcularPLComPrejuizo() {
        Custody custody = Custody.reconstruct(1L, null, "VALE3", 50,
                Money.create(new BigDecimal("70.00")), null);

        Money pl = custody.calcPl(Money.create(new BigDecimal("62.00")));

        assertEquals(0, pl.getAmount().compareTo(new BigDecimal("-400.00")));
    }

    @Test
    void deveCalcularPLPercentualComLucro() {
        Custody custody = Custody.reconstruct(1L, null, "PETR4", 68,
                Money.create(new BigDecimal("49.41")), null);

        BigDecimal plPercent = custody.calcPlPercentual(Money.create(new BigDecimal("55.00")));

        assertEquals(0, plPercent.compareTo(new BigDecimal("11.31")));
    }

    @Test
    void deveCalcularPLPercentualComPrejuizo() {
        Custody custody = Custody.reconstruct(1L, null, "VALE3", 50,
                Money.create(new BigDecimal("70.00")), null);

        BigDecimal plPercent = custody.calcPlPercentual(Money.create(new BigDecimal("62.00")));

        assertEquals(0, plPercent.compareTo(new BigDecimal("-11.43")));
    }

    @Test
    void deveRetornarZeroParaPLPercentualQuandoPrecoMedioZero() {
        Custody custody = Custody.reconstruct(1L, null, "PETR4", 0,
                Money.create(BigDecimal.ZERO), null);

        BigDecimal plPercent = custody.calcPlPercentual(Money.create(new BigDecimal("55.00")));

        assertEquals(0, plPercent.compareTo(BigDecimal.ZERO));
    }

    @Test
    void deveCalcularComposicaoPercentual() {
        Custody custody = Custody.reconstruct(1L, null, "PETR4", 68,
                Money.create(new BigDecimal("49.41")), null);

        Money currentPrice = Money.create(new BigDecimal("55.00"));
        Money portfolioTotal = Money.create(new BigDecimal("11433.25"));

        Money composition = custody.calcCompositionPercentage(currentPrice, portfolioTotal);

        assertEquals(0, composition.getAmount().compareTo(new BigDecimal("32.71")));
    }

    @Test
    void deveRetornarZeroParaComposicaoQuandoPortfolioZero() {
        Custody custody = Custody.reconstruct(1L, null, "PETR4", 68,
                Money.create(new BigDecimal("49.41")), null);

        Money composition = custody.calcCompositionPercentage(
                Money.create(new BigDecimal("55.00")),
                Money.create(BigDecimal.ZERO));

        assertEquals(0, composition.getAmount().compareTo(BigDecimal.ZERO));
    }

    @Test
    void deveAtualizarQuantidadeResidualParaMaster() {
        BrokerageAccount masterAccount = BrokerageAccount.reconstruct(
                1L, null, "MASTER", BrokerageAccountType.MASTER, null, new ArrayList<>());

        Custody custody = Custody.reconstruct(1L, masterAccount, "PETR4", 0,
                Money.create(BigDecimal.ZERO), null);

        custody.updateResidualQuantity(100, Money.create(new BigDecimal("35.00")), BrokerageAccountType.MASTER);

        assertEquals(100, custody.getQuantity());
    }

    @Test
    void naoDeveAtualizarQuantidadeResidualParaContaNaoMaster() {
        Custody custody = Custody.reconstruct(1L, null, "PETR4", 50,
                Money.create(new BigDecimal("30.00")), null);

        custody.updateResidualQuantity(100, Money.create(new BigDecimal("35.00")), BrokerageAccountType.CLIENT);

        assertEquals(50, custody.getQuantity());
    }
}
