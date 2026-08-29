package com.matheus.srv_portfolio_scheduler.application.command.PortfolioRebalance;

import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.BrokerageAccountRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.CotahistFilePort;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.RecommendedPortfolioRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.service.PortfolioPurchaseExecutionService;
import com.matheus.srv_portfolio_scheduler.application.service.PortfolioRebalanceService;
import com.matheus.srv_portfolio_scheduler.domain.entities.BrokerageAccount;
import com.matheus.srv_portfolio_scheduler.domain.entities.RecommendedPortfolio;
import com.matheus.srv_portfolio_scheduler.domain.exceptions.ActivePortfolioNotFoundException;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.PortfolioComparisonDTO;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.QuoteDTO;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.RebalanceExecutionResultDTO;
import com.matheus.srv_portfolio_scheduler.fixtures.CustomerAggregateFixture;
import com.matheus.srv_portfolio_scheduler.fixtures.QuoteFixture;
import com.matheus.srv_portfolio_scheduler.fixtures.RecommendedPortfolioFixture;
import com.matheus.srv_portfolio_scheduler.fixtures.TestDefaults;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioRebalanceHandlerTest {

    private static final Set<String> ALL_TICKERS =
            Set.of("PETR4", "VALE3", "ITUB4", "BBDC4", "WEGE3", "ABEV3", "RENT3");

    @Mock
    private CotahistFilePort cotahistFilePort;
    @Mock
    private PortfolioRebalanceService portfolioRebalanceService;
    @Mock
    private BrokerageAccountRepositoryPort brokerageAccountRepository;
    @Mock
    private RecommendedPortfolioRepositoryPort portfolioRepository;
    @Mock
    private PortfolioPurchaseExecutionService purchaseExecutionService;

    @InjectMocks
    private PortfolioRebalanceHandler handler;

    @Test
    void deveRebalancearEComprarQuandoHouverMudancaNaCesta() {
        // Arrange
        RecommendedPortfolio portfolio = RecommendedPortfolioFixture.aRecommendedPortfolio().build();
        BrokerageAccount masterAccount = CustomerAggregateFixture.aCustomerAggregate().build().getBrokerageAccount();
        PortfolioComparisonDTO comparison = comparisonWithChanges();
        List<QuoteDTO> quotes = quotesForTickers();
        RebalanceExecutionResultDTO rebalanceResult = new RebalanceExecutionResultDTO(TestDefaults.money("5000.00"), List.of());

        when(portfolioRepository.getActiveRecommendedPortfolio()).thenReturn(Optional.of(portfolio));
        when(cotahistFilePort.getTickerByLastCotahist(eq(ALL_TICKERS))).thenReturn(quotes);
        when(brokerageAccountRepository.getMasterAccount()).thenReturn(masterAccount);
        when(portfolioRebalanceService.execute(eq(masterAccount), eq(comparison), anyMap())).thenReturn(rebalanceResult);

        // Act
        PortfolioRebalanceResponse response = handler.handler(new PortfolioRebalanceCommand(comparison));

        // Assert
        assertNotNull(response);
        verify(portfolioRebalanceService).execute(eq(masterAccount), eq(comparison), anyMap());
        verify(purchaseExecutionService).executePurchase(portfolio, masterAccount, rebalanceResult, quotes);
    }

    @Test
    void deveEncerrarSemProcessarQuandoNaoHouverMudancas() {
        // Arrange
        RecommendedPortfolio portfolio = RecommendedPortfolioFixture.aRecommendedPortfolio().build();
        PortfolioComparisonDTO noChanges = new PortfolioComparisonDTO(List.of(), List.of(), List.of());
        when(portfolioRepository.getActiveRecommendedPortfolio()).thenReturn(Optional.of(portfolio));

        // Act
        PortfolioRebalanceResponse response = handler.handler(new PortfolioRebalanceCommand(noChanges));

        // Assert
        assertNotNull(response);
        verifyNoInteractions(cotahistFilePort, brokerageAccountRepository, portfolioRebalanceService, purchaseExecutionService);
    }

    @Test
    void deveLancarExcecaoQuandoCarteiraAtivaNaoExistir() {
        PortfolioComparisonDTO comparison = new PortfolioComparisonDTO(List.of(), List.of(), List.of());
        when(portfolioRepository.getActiveRecommendedPortfolio()).thenReturn(Optional.empty());

        assertThrows(ActivePortfolioNotFoundException.class,
                () -> handler.handler(new PortfolioRebalanceCommand(comparison)));

        verifyNoInteractions(cotahistFilePort, brokerageAccountRepository, portfolioRebalanceService, purchaseExecutionService);
    }

    private PortfolioComparisonDTO comparisonWithChanges() {
        return new PortfolioComparisonDTO(
                List.of(new PortfolioComparisonDTO.AlteredItem("PETR4", new BigDecimal("20"), new BigDecimal("25"))),
                List.of(new PortfolioComparisonDTO.RemovedItem("ABEV3")),
                List.of(new PortfolioComparisonDTO.NewItem("RENT3", new BigDecimal("15"))));
    }

    private List<QuoteDTO> quotesForTickers() {
        return List.of(
                QuoteFixture.aQuote().withTicker("PETR4").withClosePrice("49.41").build(),
                QuoteFixture.aQuote().withTicker("ABEV3").withClosePrice("14.20").build(),
                QuoteFixture.aQuote().withTicker("RENT3").withClosePrice("45.15").build());
    }
}
