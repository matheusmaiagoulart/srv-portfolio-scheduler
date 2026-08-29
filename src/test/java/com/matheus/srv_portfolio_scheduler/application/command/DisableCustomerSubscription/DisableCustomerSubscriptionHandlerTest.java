package com.matheus.srv_portfolio_scheduler.application.command.DisableCustomerSubscription;

import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.CustomerRepositoryPort;
import com.matheus.srv_portfolio_scheduler.domain.entities.Customer;
import com.matheus.srv_portfolio_scheduler.domain.exceptions.CustomerAlreadyInactiveException;
import com.matheus.srv_portfolio_scheduler.domain.exceptions.CustomerNotFound;
import com.matheus.srv_portfolio_scheduler.fixtures.CustomerAggregateFixture;
import com.matheus.srv_portfolio_scheduler.fixtures.TestDefaults;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisableCustomerSubscriptionHandlerTest {

    @Mock
    private CustomerRepositoryPort customerRepository;

    @InjectMocks
    private DisableCustomerSubscriptionHandler handler;

    @Test
    void deveDesativarESalvarClienteAtivo() {
        Customer customer = CustomerAggregateFixture.aCustomerAggregate().build();
        DisableCustomerSubscriptionCommand command =
                new DisableCustomerSubscriptionCommand(TestDefaults.CUSTOMER_ID);
        when(customerRepository.findById(TestDefaults.CUSTOMER_ID))
                .thenReturn(Optional.of(customer));

        DisableCustomerSubscriptionResponse response = handler.handler(command);

        assertAll(
                () -> assertFalse(customer.isActive()),
                () -> assertEquals(TestDefaults.CUSTOMER_ID, response.customerId()),
                () -> assertEquals("Customer subscription disabled successfully.", response.message()));
        verify(customerRepository).save(customer);
    }

    @Test
    void deveLancarExcecaoENaoSalvarQuandoClienteNaoExistir() {
        when(customerRepository.findById(TestDefaults.CUSTOMER_ID))
                .thenReturn(Optional.empty());

        assertThrows(CustomerNotFound.class,
                () -> handler.handler(new DisableCustomerSubscriptionCommand(TestDefaults.CUSTOMER_ID)));

        verify(customerRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoENaoSalvarQuandoClienteJaEstiverInativo() {
        Customer customer = CustomerAggregateFixture.aCustomerAggregate().inactive().build();
        when(customerRepository.findById(TestDefaults.CUSTOMER_ID))
                .thenReturn(Optional.of(customer));

        assertThrows(CustomerAlreadyInactiveException.class,
                () -> handler.handler(new DisableCustomerSubscriptionCommand(TestDefaults.CUSTOMER_ID)));

        verify(customerRepository, never()).save(any());
    }
}
