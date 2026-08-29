package com.matheus.srv_portfolio_scheduler.src.domain.services;

import com.matheus.srv_portfolio_scheduler.domain.entities.PortfolioItem;
import com.matheus.srv_portfolio_scheduler.domain.services.PortfolioComparisonService;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.PortfolioComparisonDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PortfolioComparisonServiceTest {

    private final PortfolioComparisonService service = new PortfolioComparisonService();

    @Test
    void deveIdentificarAtivoRemovido() {
        List<PortfolioItem> current = List.of(
                PortfolioItem.create("PETR4", 30),
                PortfolioItem.create("VALE3", 25),
                PortfolioItem.create("ITUB4", 20),
                PortfolioItem.create("BBDC4", 15),
                PortfolioItem.create("WEGE3", 10));

        List<PortfolioItem> incoming = List.of(
                PortfolioItem.create("PETR4", 30),
                PortfolioItem.create("VALE3", 25),
                PortfolioItem.create("ITUB4", 20),
                PortfolioItem.create("ABEV3", 15),
                PortfolioItem.create("WEGE3", 10));

        PortfolioComparisonDTO result = service.compare(current, incoming);

        assertEquals(1, result.removed().size());
        assertEquals("BBDC4", result.removed().get(0).ticker());
        assertEquals(1, result.added().size());
        assertEquals("ABEV3", result.added().get(0).ticker());
        assertTrue(result.hasChange());
    }

    @Test
    void deveIdentificarAtivoAdicionado() {
        List<PortfolioItem> current = List.of(
                PortfolioItem.create("PETR4", 25),
                PortfolioItem.create("VALE3", 25),
                PortfolioItem.create("ITUB4", 20),
                PortfolioItem.create("BBDC4", 15),
                PortfolioItem.create("WEGE3", 15));

        List<PortfolioItem> incoming = List.of(
                PortfolioItem.create("PETR4", 25),
                PortfolioItem.create("VALE3", 20),
                PortfolioItem.create("ITUB4", 20),
                PortfolioItem.create("RENT3", 20),
                PortfolioItem.create("WEGE3", 15));

        PortfolioComparisonDTO result = service.compare(current, incoming);

        assertEquals(1, result.added().size());
        assertEquals("RENT3", result.added().get(0).ticker());
        assertEquals(new BigDecimal("20"), result.added().get(0).percentage());
        assertEquals(1, result.removed().size());
        assertEquals("BBDC4", result.removed().get(0).ticker());
    }

    @Test
    void deveIdentificarAtivoComPercentualAlterado() {
        List<PortfolioItem> current = List.of(
                PortfolioItem.create("PETR4", 30),
                PortfolioItem.create("VALE3", 25),
                PortfolioItem.create("ITUB4", 20),
                PortfolioItem.create("BBDC4", 15),
                PortfolioItem.create("WEGE3", 10));

        List<PortfolioItem> incoming = List.of(
                PortfolioItem.create("PETR4", 25),
                PortfolioItem.create("VALE3", 20),
                PortfolioItem.create("ITUB4", 25),
                PortfolioItem.create("BBDC4", 15),
                PortfolioItem.create("WEGE3", 15));

        PortfolioComparisonDTO result = service.compare(current, incoming);

        assertEquals(4, result.altered().size());
        assertTrue(result.removed().isEmpty());
        assertTrue(result.added().isEmpty());
        assertTrue(result.hasChange());
    }

    @Test
    void deveIdentificarAumentoEDiminuicaoDePercentual() {
        List<PortfolioItem> current = List.of(
                PortfolioItem.create("PETR4", 30),
                PortfolioItem.create("VALE3", 20),
                PortfolioItem.create("ITUB4", 20),
                PortfolioItem.create("BBDC4", 15),
                PortfolioItem.create("WEGE3", 15));

        List<PortfolioItem> incoming = List.of(
                PortfolioItem.create("PETR4", 25),
                PortfolioItem.create("VALE3", 25),
                PortfolioItem.create("ITUB4", 20),
                PortfolioItem.create("BBDC4", 15),
                PortfolioItem.create("WEGE3", 15));

        PortfolioComparisonDTO result = service.compare(current, incoming);

        assertEquals(2, result.altered().size());

        PortfolioComparisonDTO.AlteredItem petr4 = result.altered().stream()
                .filter(a -> a.ticker().equals("PETR4")).findFirst().orElseThrow();
        assertFalse(petr4.isIncrease());

        PortfolioComparisonDTO.AlteredItem vale3 = result.altered().stream()
                .filter(a -> a.ticker().equals("VALE3")).findFirst().orElseThrow();
        assertTrue(vale3.isIncrease());
    }

    @Test
    void deveRetornarVazioQuandoCarteirasForemIdenticas() {
        List<PortfolioItem> current = List.of(
                PortfolioItem.create("PETR4", 20),
                PortfolioItem.create("VALE3", 20),
                PortfolioItem.create("ITUB4", 20),
                PortfolioItem.create("BBDC4", 20),
                PortfolioItem.create("WEGE3", 20));

        List<PortfolioItem> incoming = List.of(
                PortfolioItem.create("PETR4", 20),
                PortfolioItem.create("VALE3", 20),
                PortfolioItem.create("ITUB4", 20),
                PortfolioItem.create("BBDC4", 20),
                PortfolioItem.create("WEGE3", 20));

        PortfolioComparisonDTO result = service.compare(current, incoming);

        assertTrue(result.altered().isEmpty());
        assertTrue(result.removed().isEmpty());
        assertTrue(result.added().isEmpty());
        assertFalse(result.hasChange());
    }

    @Test
    void deveRetornarTodosOsTickersAfetados() {
        List<PortfolioItem> current = List.of(
                PortfolioItem.create("PETR4", 30),
                PortfolioItem.create("VALE3", 25),
                PortfolioItem.create("ITUB4", 20),
                PortfolioItem.create("BBDC4", 15),
                PortfolioItem.create("WEGE3", 10));

        List<PortfolioItem> incoming = List.of(
                PortfolioItem.create("PETR4", 25),
                PortfolioItem.create("VALE3", 20),
                PortfolioItem.create("ITUB4", 20),
                PortfolioItem.create("ABEV3", 20),
                PortfolioItem.create("RENT3", 15));

        PortfolioComparisonDTO result = service.compare(current, incoming);

        var allTickers = result.getAllTickersName();
        assertTrue(allTickers.contains("PETR4"));
        assertTrue(allTickers.contains("VALE3"));
        assertTrue(allTickers.contains("BBDC4"));
        assertTrue(allTickers.contains("WEGE3"));
        assertTrue(allTickers.contains("ABEV3"));
        assertTrue(allTickers.contains("RENT3"));
    }
}
