package com.matheus.srv_portfolio_scheduler.src.domain.services.rebalance;

import com.matheus.srv_portfolio_scheduler.domain.entities.Custody;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.RebalanceResultDTO;
import com.matheus.srv_portfolio_scheduler.domain.services.rebalance.SellRemovedAssetsOperation;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SellRemovedAssetsOperationTest {

    private final SellRemovedAssetsOperation operation = new SellRemovedAssetsOperation();

    @Test
    void deveVenderTodaPosicaoDeAtivosRemovidos() {
        Map<String, Custody> custodies = new HashMap<>();
        custodies.put("BBDC4", createCustody("BBDC4", 10, "14.00"));
        custodies.put("WEGE3", createCustody("WEGE3", 5, "38.00"));
        custodies.put("PETR4", createCustody("PETR4", 8, "35.00"));

        Set<String> removedTickers = Set.of("BBDC4", "WEGE3");

        Map<String, Money> prices = Map.of(
                "BBDC4", Money.create(new BigDecimal("15.00")),
                "WEGE3", Money.create(new BigDecimal("40.00")));

        RebalanceResultDTO result = operation.execute(custodies, removedTickers, prices, 1L);

        assertEquals(new BigDecimal("350.00"), result.totalReleased().getAmount());
        assertEquals(2, result.auditRecords().size());
        assertEquals(0, custodies.get("BBDC4").getQuantity());
        assertEquals(0, custodies.get("WEGE3").getQuantity());
        assertEquals(8, custodies.get("PETR4").getQuantity());
    }

    @Test
    void deveIgnorarAtivoRemovidoSemPosicao() {
        Map<String, Custody> custodies = new HashMap<>();
        custodies.put("BBDC4", createCustody("BBDC4", 0, "14.00"));

        Set<String> removedTickers = Set.of("BBDC4");
        Map<String, Money> prices = Map.of("BBDC4", Money.create(new BigDecimal("15.00")));

        RebalanceResultDTO result = operation.execute(custodies, removedTickers, prices, 1L);

        assertTrue(result.totalReleased().getAmount().compareTo(BigDecimal.ZERO) == 0);
        assertTrue(result.auditRecords().isEmpty());
    }

    @Test
    void deveIgnorarAtivoRemovidoSemCustodia() {
        Map<String, Custody> custodies = new HashMap<>();

        Set<String> removedTickers = Set.of("BBDC4");
        Map<String, Money> prices = Map.of("BBDC4", Money.create(new BigDecimal("15.00")));

        RebalanceResultDTO result = operation.execute(custodies, removedTickers, prices, 1L);

        assertTrue(result.totalReleased().getAmount().compareTo(BigDecimal.ZERO) == 0);
        assertTrue(result.auditRecords().isEmpty());
    }

    @Test
    void deveIgnorarAtivoSemPreco() {
        Map<String, Custody> custodies = new HashMap<>();
        custodies.put("BBDC4", createCustody("BBDC4", 10, "14.00"));

        Set<String> removedTickers = Set.of("BBDC4");
        Map<String, Money> prices = Map.of();

        RebalanceResultDTO result = operation.execute(custodies, removedTickers, prices, 1L);

        assertTrue(result.totalReleased().getAmount().compareTo(BigDecimal.ZERO) == 0);
        assertEquals(10, custodies.get("BBDC4").getQuantity());
    }

    private Custody createCustody(String ticker, int quantity, String averagePrice) {
        return Custody.reconstruct(1L, null, ticker, quantity, Money.create(new BigDecimal(averagePrice)), null);
    }
}
