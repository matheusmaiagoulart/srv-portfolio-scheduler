package com.matheus.srv_portfolio_scheduler.src.domain.entities;

import com.matheus.srv_portfolio_scheduler.domain.entities.PortfolioItem;
import com.matheus.srv_portfolio_scheduler.domain.entities.RecommendedPortfolio;
import com.matheus.srv_portfolio_scheduler.domain.exceptions.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecommendedPortfolioTest {

    @Test
    void deveCriarPortfolioRecomendadoComCincoItens() {
        List<PortfolioItem> items = createValidItems();

        RecommendedPortfolio portfolio = RecommendedPortfolio.create("Carteira Conservadora", items, null);

        assertEquals("Carteira Conservadora", portfolio.getName());
        assertTrue(portfolio.isActive());
        assertEquals(5, portfolio.getPortfolioItems().size());
        assertNotNull(portfolio.getCreatedAt());
    }

    @Test
    void deveLancarExcecaoQuandoNomeNulo() {
        List<PortfolioItem> items = createValidItems();

        BusinessException ex = assertThrows(BusinessException.class,
                () -> RecommendedPortfolio.create(null, items, null));

        assertEquals("INVALID_PORTFOLIO_NAME", ex.getErrorCode());
    }

    @Test
    void deveLancarExcecaoQuandoNomeVazio() {
        List<PortfolioItem> items = createValidItems();

        BusinessException ex = assertThrows(BusinessException.class,
                () -> RecommendedPortfolio.create("   ", items, null));

        assertEquals("INVALID_PORTFOLIO_NAME", ex.getErrorCode());
    }

    @Test
    void deveLancarExcecaoQuandoItensNulo() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> RecommendedPortfolio.create("Portfolio", null, null));

        assertEquals("INVALID_PORTFOLIO_ITEMS", ex.getErrorCode());
    }

    @Test
    void deveLancarExcecaoQuandoMenosDeCincoItens() {
        List<PortfolioItem> items = List.of(
                PortfolioItem.create("PETR4", 50),
                PortfolioItem.create("VALE3", 50));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> RecommendedPortfolio.create("Portfolio", items, null));

        assertEquals("INVALID_PORTFOLIO_ITEMS", ex.getErrorCode());
    }

    @Test
    void deveLancarExcecaoQuandoMaisDeCincoItens() {
        List<PortfolioItem> items = List.of(
                PortfolioItem.create("PETR4", 20),
                PortfolioItem.create("VALE3", 20),
                PortfolioItem.create("ITUB4", 15),
                PortfolioItem.create("BBDC4", 15),
                PortfolioItem.create("WEGE3", 15),
                PortfolioItem.create("ABEV3", 15));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> RecommendedPortfolio.create("Portfolio", items, null));

        assertEquals("INVALID_PORTFOLIO_ITEMS", ex.getErrorCode());
    }

    @Test
    void deveLancarExcecaoQuandoTickersDuplicados() {
        List<PortfolioItem> items = List.of(
                PortfolioItem.create("PETR4", 30),
                PortfolioItem.create("PETR4", 25),
                PortfolioItem.create("ITUB4", 20),
                PortfolioItem.create("BBDC4", 15),
                PortfolioItem.create("WEGE3", 10));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> RecommendedPortfolio.create("Portfolio", items, null));

        assertEquals("DUPLICATED_PORTFOLIO_ITEMS", ex.getErrorCode());
    }

    @Test
    void deveLancarExcecaoQuandoTotalNaoSoma100() {
        List<PortfolioItem> items = List.of(
                PortfolioItem.create("PETR4", 30),
                PortfolioItem.create("VALE3", 25),
                PortfolioItem.create("ITUB4", 20),
                PortfolioItem.create("BBDC4", 15),
                PortfolioItem.create("WEGE3", 5));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> RecommendedPortfolio.create("Portfolio", items, null));

        assertEquals("INVALID_TOTAL_PERCENTAGE", ex.getErrorCode());
    }

    @Test
    void deveDesativarPortfolio() {
        List<PortfolioItem> items = createValidItems();
        RecommendedPortfolio portfolio = RecommendedPortfolio.create("Carteira", items, null);

        portfolio.deactivate();

        assertFalse(portfolio.isActive());
        assertNotNull(portfolio.getTerminationDate());
    }

    @Test
    void deveAssociarItensAoPortfolio() {
        List<PortfolioItem> items = createValidItems();

        RecommendedPortfolio portfolio = RecommendedPortfolio.create("Carteira", items, null);

        assertTrue(portfolio.getPortfolioItems().stream()
                .allMatch(item -> item.getRecommendedPortfolio() == portfolio));
    }

    @Test
    void deveAceitarPercentuaisVariados() {
        List<PortfolioItem> items = List.of(
                PortfolioItem.create("PETR4", 40),
                PortfolioItem.create("VALE3", 30),
                PortfolioItem.create("ITUB4", 15),
                PortfolioItem.create("BBDC4", 10),
                PortfolioItem.create("WEGE3", 5));

        RecommendedPortfolio portfolio = RecommendedPortfolio.create("Agressiva", items, null);

        assertEquals(5, portfolio.getPortfolioItems().size());

        int total = portfolio.getPortfolioItems().stream()
                .mapToInt(PortfolioItem::getPercentage)
                .sum();
        assertEquals(100, total);
    }

    private List<PortfolioItem> createValidItems() {
        return List.of(
                PortfolioItem.create("PETR4", 30),
                PortfolioItem.create("VALE3", 25),
                PortfolioItem.create("ITUB4", 20),
                PortfolioItem.create("BBDC4", 15),
                PortfolioItem.create("WEGE3", 10));
    }
}
