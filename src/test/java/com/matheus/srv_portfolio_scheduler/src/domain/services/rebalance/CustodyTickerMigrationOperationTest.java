package com.matheus.srv_portfolio_scheduler.src.domain.services.rebalance;

import com.matheus.srv_portfolio_scheduler.domain.entities.Custody;
import com.matheus.srv_portfolio_scheduler.domain.services.rebalance.CustodyTickerMigrationOperation;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CustodyTickerMigrationOperationTest {

    private CustodyTickerMigrationOperation operation;

    @BeforeEach
    void setup() {
        operation = new CustodyTickerMigrationOperation();
    }

    @Test
    void deveMigrarTickerDeUmAtivoParaOutro() {
        Map<String, Custody> custodies = new HashMap<>();
        custodies.put("ITUB3", Custody.reconstruct(1L, null, "ITUB3", 100,
                Money.create(new BigDecimal("25.00")), null));

        Set<String> removedTickers = new LinkedHashSet<>(List.of("ITUB3"));
        Set<String> addedTickers = new LinkedHashSet<>(List.of("ITUB4"));

        operation.execute(custodies, removedTickers, addedTickers);

        assertEquals("ITUB4", custodies.get("ITUB3").getTicker());
        assertEquals(100, custodies.get("ITUB3").getQuantity());
    }

    @Test
    void deveMigrarMultiplosTickersNaOrdem() {
        Map<String, Custody> custodies = new HashMap<>();
        custodies.put("BBDC3", Custody.reconstruct(1L, null, "BBDC3", 50,
                Money.create(new BigDecimal("12.00")), null));
        custodies.put("ITUB3", Custody.reconstruct(2L, null, "ITUB3", 80,
                Money.create(new BigDecimal("25.00")), null));

        Set<String> removedTickers = new LinkedHashSet<>(List.of("BBDC3", "ITUB3"));
        Set<String> addedTickers = new LinkedHashSet<>(List.of("BBDC4", "ITUB4"));

        operation.execute(custodies, removedTickers, addedTickers);

        assertEquals("BBDC4", custodies.get("BBDC3").getTicker());
        assertEquals("ITUB4", custodies.get("ITUB3").getTicker());
    }

    @Test
    void deveIgnorarQuandoCustodiaNaoExiste() {
        Map<String, Custody> custodies = new HashMap<>();
        custodies.put("PETR4", Custody.reconstruct(1L, null, "PETR4", 100,
                Money.create(new BigDecimal("35.00")), null));

        Set<String> removedTickers = new LinkedHashSet<>(List.of("VALE3"));
        Set<String> addedTickers = new LinkedHashSet<>(List.of("VALE5"));

        assertDoesNotThrow(() -> operation.execute(custodies, removedTickers, addedTickers));

        assertEquals("PETR4", custodies.get("PETR4").getTicker());
    }

    @Test
    void deveManterQuantidadeEPrecoMedioAposMigracao() {
        Map<String, Custody> custodies = new HashMap<>();
        Custody original = Custody.reconstruct(1L, null, "BBAS3", 200,
                Money.create(new BigDecimal("28.50")), null);
        custodies.put("BBAS3", original);

        Set<String> removedTickers = new LinkedHashSet<>(List.of("BBAS3"));
        Set<String> addedTickers = new LinkedHashSet<>(List.of("BBAS4"));

        operation.execute(custodies, removedTickers, addedTickers);

        assertEquals("BBAS4", original.getTicker());
        assertEquals(200, original.getQuantity());
        assertEquals(0, original.getAveragePrice().getAmount().compareTo(new BigDecimal("28.50")));
    }

    @Test
    void deveExecutarSemErroComListasVazias() {
        Map<String, Custody> custodies = new HashMap<>();
        custodies.put("PETR4", Custody.reconstruct(1L, null, "PETR4", 100,
                Money.create(new BigDecimal("35.00")), null));

        Set<String> removedTickers = new LinkedHashSet<>();
        Set<String> addedTickers = new LinkedHashSet<>();

        assertDoesNotThrow(() -> operation.execute(custodies, removedTickers, addedTickers));
    }
}
