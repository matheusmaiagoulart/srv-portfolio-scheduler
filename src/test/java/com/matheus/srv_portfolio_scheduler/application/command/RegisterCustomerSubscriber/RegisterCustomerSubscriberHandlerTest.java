package com.matheus.srv_portfolio_scheduler.application.command.RegisterCustomerSubscriber;

import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.BrokerageAccountRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.RecommendedPortfolioRepositoryPort;
import com.matheus.srv_portfolio_scheduler.domain.entities.BrokerageAccount;
import com.matheus.srv_portfolio_scheduler.domain.entities.Customer;
import com.matheus.srv_portfolio_scheduler.domain.entities.RecommendedPortfolio;
import com.matheus.srv_portfolio_scheduler.domain.exceptions.ActivePortfolioNotFoundException;
import com.matheus.srv_portfolio_scheduler.fixtures.CustomerAggregateFixture;
import com.matheus.srv_portfolio_scheduler.fixtures.RecommendedPortfolioFixture;
import com.matheus.srv_portfolio_scheduler.fixtures.TestDefaults;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterCustomerSubscriberHandlerTest {

    @Mock
    private BrokerageAccountRepositoryPort brokerageAccountRepositoryPort;

    @Mock
    private RecommendedPortfolioRepositoryPort recommendedPortfolioRepositoryPort;

    @InjectMocks
    private RegisterCustomerSubscriberHandler registerCustomerSubscriberHandler;

    @Test
    void deveCadastrarClienteQuandoExistirCarteiraAtiva() {
        // Arrange
        RecommendedPortfolio portfolio = RecommendedPortfolioFixture.aRecommendedPortfolio().build();
        Customer persistedCustomer = CustomerAggregateFixture.aCustomerAggregate().build();
        BrokerageAccount persistedAccount = persistedCustomer.getBrokerageAccount();
        RegisterCustomerSubscriberCommand command = new RegisterCustomerSubscriberCommand(
                TestDefaults.CUSTOMER_NAME,
                TestDefaults.CPF,
                TestDefaults.EMAIL,
                TestDefaults.MONTHLY_AMOUNT);

        when(recommendedPortfolioRepositoryPort.getActiveRecommendedPortfolio())
                .thenReturn(Optional.of(portfolio));
        when(brokerageAccountRepositoryPort.save(any(BrokerageAccount.class)))
                .thenReturn(persistedAccount);

        // Act
        RegisterCustomerSubscriberResponse response = registerCustomerSubscriberHandler.handler(command);

        // Assert
        assertEquals(TestDefaults.BROKERAGE_ACCOUNT_ID, response.id());
        assertEquals(TestDefaults.CUSTOMER_NAME, response.name());
        verify(brokerageAccountRepositoryPort).save(any(BrokerageAccount.class));
    }

    @Test
    void deveLancarExcecaoQuandoNaoExistirCarteiraAtiva() {
        // Arrange
        RegisterCustomerSubscriberCommand command = new RegisterCustomerSubscriberCommand(
                TestDefaults.CUSTOMER_NAME,
                TestDefaults.CPF,
                TestDefaults.EMAIL,
                TestDefaults.MONTHLY_AMOUNT);
        when(recommendedPortfolioRepositoryPort.getActiveRecommendedPortfolio())
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(ActivePortfolioNotFoundException.class,
                () -> registerCustomerSubscriberHandler.handler(command));

        verifyNoInteractions(brokerageAccountRepositoryPort);
    }
}



