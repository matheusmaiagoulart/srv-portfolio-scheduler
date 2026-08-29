package com.matheus.srv_portfolio_scheduler.src.domain.services;

import com.matheus.srv_portfolio_scheduler.domain.entities.Custody;
import com.matheus.srv_portfolio_scheduler.domain.services.PortfolioAnalytics;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PortfolioAnalyticsTest {

    @Test
    void deveCalcularValorTotalInvestido() {
        List<Custody> custodies = List.of(
                createCustody("PETR4", 8, "35.00"),
                createCustody("VALE3", 4, "62.00"));

        Money totalInvested = PortfolioAnalytics.calcTotalAmountInvested(custodies);

        assertEquals(new BigDecimal("528.00"), totalInvested.getAmount());
    }

    @Test
    void deveCalcularValorAtualDoPortfolio() {
        List<Custody> custodies = List.of(
                createCustody("PETR4", 8, "35.00"),
                createCustody("VALE3", 4, "62.00"));

        Map<String, Money> currentPrices = Map.of(
                "PETR4", Money.create(new BigDecimal("40.00")),
                "VALE3", Money.create(new BigDecimal("70.00")));

        Money portfolioValue = PortfolioAnalytics.calcPortfolioTotalValue(custodies, currentPrices);

        assertEquals(new BigDecimal("600.00"), portfolioValue.getAmount());
    }

    @Test
    void deveCalcularPLTotalComLucro() {
        List<Custody> custodies = List.of(
                createCustody("PETR4", 8, "35.00"),
                createCustody("VALE3", 4, "62.00"));

        Map<String, Money> currentPrices = Map.of(
                "PETR4", Money.create(new BigDecimal("40.00")),
                "VALE3", Money.create(new BigDecimal("70.00")));

        Money plTotal = PortfolioAnalytics.calcPlTotal(custodies, currentPrices);

        assertEquals(new BigDecimal("72.00"), plTotal.getAmount());
    }

    @Test
    void deveCalcularPLTotalComPrejuizo() {
        List<Custody> custodies = List.of(
                createCustody("PETR4", 8, "50.00"),
                createCustody("VALE3", 4, "80.00"));

        Map<String, Money> currentPrices = Map.of(
                "PETR4", Money.create(new BigDecimal("40.00")),
                "VALE3", Money.create(new BigDecimal("70.00")));

        assertThrows(
                com.matheus.srv_portfolio_scheduler.domain.exceptions.BusinessException.class,
                () -> PortfolioAnalytics.calcPlTotal(custodies, currentPrices)
        );
    }

    @Test
    void deveCalcularRentabilidadePercentualPositiva() {
        Money invested = Money.create(new BigDecimal("1000.00"));
        Money currentValue = Money.create(new BigDecimal("1075.00"));

        BigDecimal profitability = PortfolioAnalytics.calcPortfolioProfitability(currentValue, invested);

        assertEquals(new BigDecimal("7.50"), profitability);
    }

    @Test
    void deveCalcularRentabilidadePercentualNegativa() {
        Money invested = Money.create(new BigDecimal("1000.00"));
        Money currentValue = Money.create(new BigDecimal("850.00"));

        BigDecimal profitability = PortfolioAnalytics.calcPortfolioProfitability(currentValue, invested);

        assertEquals(new BigDecimal("-15.00"), profitability);
    }

    @Test
    void deveRetornarZeroQuandoInvestimentoForZero() {
        Money invested = Money.create(BigDecimal.ZERO);
        Money currentValue = Money.create(new BigDecimal("100.00"));

        BigDecimal profitability = PortfolioAnalytics.calcPortfolioProfitability(currentValue, invested);

        assertEquals(BigDecimal.ZERO, profitability);
    }

    @Test
    void deveCalcularComExemploDoDocumento() {
        Money invested = Money.create(new BigDecimal("6000.00"));
        Money currentValue = Money.create(new BigDecimal("6450.00"));

        BigDecimal profitability = PortfolioAnalytics.calcPortfolioProfitability(currentValue, invested);

        assertEquals(new BigDecimal("7.50"), profitability);
    }

    private Custody createCustody(String ticker, int quantity, String averagePrice) {
        return Custody.reconstruct(
                1L,
                null,
                ticker,
                quantity,
                Money.create(new BigDecimal(averagePrice)),
                null);
    }
}
