package com.matheus.srv_portfolio_scheduler.src.domain.services.rebalance;

import com.matheus.srv_portfolio_scheduler.domain.entities.Custody;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.RebalanceResultDTO;
import com.matheus.srv_portfolio_scheduler.domain.services.rebalance.SellExcessOperation;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SellExcessOperationTest {

    private final SellExcessOperation operation = new SellExcessOperation();

    @Test
    void deveVenderExcessoQuandoPercentualDiminuiu() {
        Map<String, Custody> custodies = new HashMap<>();
        custodies.put("PETR4", createCustody("PETR4", 8, "35.00"));

        Map<String, BigDecimal> alteredAssets = Map.of("PETR4", new BigDecimal("25"));

        Map<String, Money> prices = Map.of("PETR4", Money.create(new BigDecimal("40.00")));

        Money originalPortfolioValue = Money.create(new BigDecimal("1000.00"));

        RebalanceResultDTO result = operation.execute(custodies, alteredAssets, prices, 1L, originalPortfolioValue);

        assertEquals(new BigDecimal("80.00"), result.totalReleased().getAmount());
        assertEquals(6, custodies.get("PETR4").getQuantity());
        assertEquals(1, result.auditRecords().size());
    }

    @Test
    void naoDeveVenderQuandoPercentualAumentou() {
        Map<String, Custody> custodies = new HashMap<>();
        custodies.put("PETR4", createCustody("PETR4", 6, "35.00"));

        Map<String, BigDecimal> alteredAssets = Map.of("PETR4", new BigDecimal("25"));

        Map<String, Money> prices = Map.of("PETR4", Money.create(new BigDecimal("40.00")));

        Money originalPortfolioValue = Money.create(new BigDecimal("960.00"));

        RebalanceResultDTO result = operation.execute(custodies, alteredAssets, prices, 1L, originalPortfolioValue);

        assertTrue(result.totalReleased().getAmount().compareTo(BigDecimal.ZERO) == 0);
        assertEquals(6, custodies.get("PETR4").getQuantity());
        assertTrue(result.auditRecords().isEmpty());
    }

    @Test
    void deveVenderExcessoDeMultiplosAtivos() {
        Map<String, Custody> custodies = new HashMap<>();
        custodies.put("PETR4", createCustody("PETR4", 10, "35.00"));
        custodies.put("VALE3", createCustody("VALE3", 8, "60.00"));

        Map<String, BigDecimal> alteredAssets = Map.of(
                "PETR4", new BigDecimal("25"),
                "VALE3", new BigDecimal("20"));

        Map<String, Money> prices = Map.of(
                "PETR4", Money.create(new BigDecimal("40.00")),
                "VALE3", Money.create(new BigDecimal("65.00")));

        Money originalPortfolioValue = Money.create(new BigDecimal("2000.00"));

        RebalanceResultDTO result = operation.execute(custodies, alteredAssets, prices, 1L, originalPortfolioValue);

        assertTrue(result.totalReleased().getAmount().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(result.auditRecords().size() >= 1);
    }

    @Test
    void deveIgnorarAtivoSemCustodia() {
        Map<String, Custody> custodies = new HashMap<>();

        Map<String, BigDecimal> alteredAssets = Map.of("PETR4", new BigDecimal("25"));
        Map<String, Money> prices = Map.of("PETR4", Money.create(new BigDecimal("40.00")));
        Money originalPortfolioValue = Money.create(new BigDecimal("1000.00"));

        RebalanceResultDTO result = operation.execute(custodies, alteredAssets, prices, 1L, originalPortfolioValue);

        assertTrue(result.totalReleased().getAmount().compareTo(BigDecimal.ZERO) == 0);
        assertTrue(result.auditRecords().isEmpty());
    }

    @Test
    void deveIgnorarAtivoSemPreco() {
        Map<String, Custody> custodies = new HashMap<>();
        custodies.put("PETR4", createCustody("PETR4", 10, "35.00"));

        Map<String, BigDecimal> alteredAssets = Map.of("PETR4", new BigDecimal("25"));
        Map<String, Money> prices = Map.of();
        Money originalPortfolioValue = Money.create(new BigDecimal("1000.00"));

        RebalanceResultDTO result = operation.execute(custodies, alteredAssets, prices, 1L, originalPortfolioValue);

        assertTrue(result.totalReleased().getAmount().compareTo(BigDecimal.ZERO) == 0);
        assertEquals(10, custodies.get("PETR4").getQuantity());
    }

    private Custody createCustody(String ticker, int quantity, String averagePrice) {
        return Custody.reconstruct(1L, null, ticker, quantity, Money.create(new BigDecimal(averagePrice)), null);
    }
}
