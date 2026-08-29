package com.matheus.srv_portfolio_scheduler.application.command.UpdateMonthlyAmount;

import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.CustomerRepositoryPort;
import com.matheus.srv_portfolio_scheduler.domain.entities.Customer;
import com.matheus.srv_portfolio_scheduler.domain.exceptions.BusinessException;
import com.matheus.srv_portfolio_scheduler.domain.exceptions.CustomerNotFound;
import com.matheus.srv_portfolio_scheduler.fixtures.CustomerAggregateFixture;
import com.matheus.srv_portfolio_scheduler.fixtures.TestDefaults;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateMonthlyAmountHandlerTest {

    @Mock
    private CustomerRepositoryPort customerRepository;

    @InjectMocks
    private UpdateMonthlyAmountHandler handler;

    @Test
    void deveAtualizarAporteMensalEPreservarValorAnteriorNaResposta() {
        Customer customer = CustomerAggregateFixture.aCustomerAggregate().build();
        when(customerRepository.findById(TestDefaults.CUSTOMER_ID))
                .thenReturn(Optional.of(customer));

        UpdateMonthlyAmountResponse response = handler.handler(
                TestDefaults.CUSTOMER_ID,
                new UpdateMonthlyAmountCommand(1500.00));

        assertAll(
                () -> assertEquals(0, TestDefaults.MONTHLY_AMOUNT.compareTo(response.lastMonthlyAmount().getAmount())),
                () -> assertEquals(0, new BigDecimal("1500.00").compareTo(response.newMonthlyAmount().getAmount())),
                () -> assertEquals(0, new BigDecimal("1500.00").compareTo(customer.getMonthlyAmount().getAmount())),
                () -> assertEquals(TestDefaults.CUSTOMER_ID, response.customerId()));
        verify(customerRepository).save(customer);
    }

    @Test
    void deveLancarExcecaoENaoSalvarQuandoClienteNaoExistir() {
        when(customerRepository.findById(TestDefaults.CUSTOMER_ID))
                .thenReturn(Optional.empty());

        assertThrows(CustomerNotFound.class,
                () -> handler.handler(TestDefaults.CUSTOMER_ID, new UpdateMonthlyAmountCommand(1500.00)));

        verify(customerRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoENaoSalvarQuandoNovoAporteForMenorQueMinimo() {
        Customer customer = CustomerAggregateFixture.aCustomerAggregate().build();
        when(customerRepository.findById(TestDefaults.CUSTOMER_ID))
                .thenReturn(Optional.of(customer));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> handler.handler(TestDefaults.CUSTOMER_ID, new UpdateMonthlyAmountCommand(99.99)));

        assertAll(
                () -> assertEquals("INVALID_MONTHLY_AMOUNT", exception.getErrorCode()),
                () -> assertEquals(0, TestDefaults.MONTHLY_AMOUNT.compareTo(customer.getMonthlyAmount().getAmount())));
        verify(customerRepository, never()).save(any());
    }
}
