package com.matheus.srv_portfolio_scheduler.application.command.ExecutePortfolioPurchase;

import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.BrokerageAccountRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.CustomerRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.RecommendedPortfolioRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.ports.output.queries.RedisCachePort;
import com.matheus.srv_portfolio_scheduler.application.service.PortfolioPurchaseExecutionService;
import com.matheus.srv_portfolio_scheduler.domain.entities.BrokerageAccount;
import com.matheus.srv_portfolio_scheduler.domain.entities.RecommendedPortfolio;
import com.matheus.srv_portfolio_scheduler.domain.exceptions.ActivePortfolioNotFoundException;
import com.matheus.srv_portfolio_scheduler.domain.exceptions.CotahistNotFoundException;
import com.matheus.srv_portfolio_scheduler.domain.exceptions.IllegalPurchaseDayException;
import com.matheus.srv_portfolio_scheduler.domain.services.PurchaseExecutionDateValidator;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.PurchaseSummaryDTO;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.QuoteDTO;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import com.matheus.srv_portfolio_scheduler.fixtures.CustomerAggregateFixture;
import com.matheus.srv_portfolio_scheduler.fixtures.QuoteFixture;
import com.matheus.srv_portfolio_scheduler.fixtures.RecommendedPortfolioFixture;
import com.matheus.srv_portfolio_scheduler.fixtures.TestDefaults;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExecutePortfolioPurchaseHandlerTest {

    @Mock
    private RedisCachePort redisCachePort;
    @Mock
    private CustomerRepositoryPort customerRepository;
    @Mock
    private BrokerageAccountRepositoryPort brokerageRepository;
    @Mock
    private RecommendedPortfolioRepositoryPort recommendedPortfolioRepository;
    @Mock
    private PortfolioPurchaseExecutionService purchaseExecutionService;
    @Mock
    private PurchaseExecutionDateValidator purchaseExecutionDateValidator;

    @InjectMocks
    private ExecutePortfolioPurchaseHandler handler;

    @Test
    void deveExecutarCompraEConstruirRespostaComResumoDaDistribuicao() {
        // Arrange
        RecommendedPortfolio portfolio = RecommendedPortfolioFixture.aRecommendedPortfolio().build();
        BrokerageAccount masterAccount = CustomerAggregateFixture.aCustomerAggregate().build().getBrokerageAccount();
        Money customersAmount = TestDefaults.money("25000.00");
        List<QuoteDTO> quotes = List.of(QuoteFixture.aQuote().build());
        PurchaseSummaryDTO summary = new PurchaseSummaryDTO(List.of(), List.of(), 25, 100, 100);

        doNothing().when(purchaseExecutionDateValidator).validate(any(LocalDate.class));
        when(recommendedPortfolioRepository.getActiveRecommendedPortfolio()).thenReturn(Optional.of(portfolio));
        when(brokerageRepository.getMasterAccount()).thenReturn(masterAccount);
        when(customerRepository.getThirdAmountOfAllActiveCustomers()).thenReturn(customersAmount);
        when(purchaseExecutionService.executePurchase(portfolio, masterAccount, customersAmount, quotes))
                .thenReturn(summary);

        // Act
        ExecutePortfolioPurchaseResponse response = handler.handler(new ExecutePortfolioPurchaseCommand(quotes));

        // Assert
        assertEquals(25, response.totalCustomersProcessed());
        assertEquals(100, response.totalDeliveries());
        assertNotNull(response.executionDate());
        verify(purchaseExecutionDateValidator).validate(any(LocalDate.class));
        verify(purchaseExecutionService).executePurchase(portfolio, masterAccount, customersAmount, quotes);
        var executionOrder = inOrder(purchaseExecutionService, redisCachePort);
        executionOrder.verify(purchaseExecutionService).executePurchase(portfolio, masterAccount, customersAmount, quotes);
        executionOrder.verify(redisCachePort).invalidateCacheForCustomersPortfolios();
    }

    @Test
    void deveLancarExcecaoQuandoDataDeExecucaoForInvalida() {
        doThrow(new IllegalPurchaseDayException()).when(purchaseExecutionDateValidator).validate(any(LocalDate.class));

        assertThrows(IllegalPurchaseDayException.class,
                () -> handler.handler(new ExecutePortfolioPurchaseCommand(List.of(QuoteFixture.aQuote().build()))));

        verifyNoInteractions(recommendedPortfolioRepository, brokerageRepository, customerRepository, purchaseExecutionService);
    }

    @Test
    void deveLancarExcecaoQuandoCotahistEstiverVazio() {
        doNothing().when(purchaseExecutionDateValidator).validate(any(LocalDate.class));

        assertThrows(CotahistNotFoundException.class,
                () -> handler.handler(new ExecutePortfolioPurchaseCommand(List.of())));

        verifyNoInteractions(recommendedPortfolioRepository, brokerageRepository, customerRepository, purchaseExecutionService);
    }

    @Test
    void deveLancarExcecaoQuandoCarteiraAtivaNaoExistir() {
        List<QuoteDTO> quotes = List.of(QuoteFixture.aQuote().build());
        doNothing().when(purchaseExecutionDateValidator).validate(any(LocalDate.class));
        when(recommendedPortfolioRepository.getActiveRecommendedPortfolio()).thenReturn(Optional.empty());

        assertThrows(ActivePortfolioNotFoundException.class,
                () -> handler.handler(new ExecutePortfolioPurchaseCommand(quotes)));

        verifyNoInteractions(brokerageRepository, customerRepository, purchaseExecutionService);
    }
}
