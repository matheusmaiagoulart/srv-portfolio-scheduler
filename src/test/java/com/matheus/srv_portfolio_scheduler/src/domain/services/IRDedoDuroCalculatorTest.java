package com.matheus.srv_portfolio_scheduler.src.domain.services;

import com.matheus.srv_portfolio_scheduler.domain.entities.BrokerageAccount;
import com.matheus.srv_portfolio_scheduler.domain.entities.Customer;
import com.matheus.srv_portfolio_scheduler.domain.entities.Delivery;
import com.matheus.srv_portfolio_scheduler.domain.events.IRDedoDuroEvent;
import com.matheus.srv_portfolio_scheduler.domain.services.IRDedoDuroCalculator;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.CustodyPurchaseDataDTO;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IRDedoDuroCalculatorTest {

    private IRDedoDuroCalculator calculator;

    @BeforeEach
    void setup() {
        calculator = new IRDedoDuroCalculator();
        ReflectionTestUtils.setField(calculator, "aliquota", new BigDecimal("0.00005")); // 0.005%
    }

    @Test
    void deveCalcularIRDedoDuroCorretamente() {
        List<Delivery> deliveries = List.of(
                Delivery.createDelivery(1L, 1L, "PETR4", 8, Money.create(new BigDecimal("35.00"))));

        Map<Long, CustodyPurchaseDataDTO> customers = createCustomersMap(1L, "12345678901");

        List<IRDedoDuroEvent> events = calculator.calculate(deliveries, customers);

        assertEquals(1, events.size());
        IRDedoDuroEvent event = events.get(0);
        assertEquals("PETR4", event.ticker());
        assertEquals(new BigDecimal("280.00"), event.operationValue());
        assertEquals(new BigDecimal("0.01"), event.valueIR());
        assertEquals("COMPRA", event.operationType());
    }

    @Test
    void deveCalcularIRParaMultiplasEntregas() {
        List<Delivery> deliveries = List.of(
                Delivery.createDelivery(1L, 1L, "PETR4", 8, Money.create(new BigDecimal("35.00"))),
                Delivery.createDelivery(2L, 1L, "VALE3", 4, Money.create(new BigDecimal("62.00"))));

        Map<Long, CustodyPurchaseDataDTO> customers = createCustomersMap(1L, "12345678901");

        List<IRDedoDuroEvent> events = calculator.calculate(deliveries, customers);

        assertEquals(2, events.size());
    }

    @Test
    void devePreencherTodosOsCamposDoEvento() {
        List<Delivery> deliveries = List.of(
                Delivery.createDelivery(1L, 1L, "PETR4", 10, Money.create(new BigDecimal("49.41"))));

        Map<Long, CustodyPurchaseDataDTO> customers = createCustomersMap(1L, "10230456077");

        List<IRDedoDuroEvent> events = calculator.calculate(deliveries, customers);

        IRDedoDuroEvent event = events.get(0);
        assertEquals("IR_DEDO_DURO", event.type());
        assertEquals("10230456077", event.cpf());
        assertEquals("PETR4", event.ticker());
        assertEquals(10, event.quantity());
        assertEquals(new BigDecimal("49.41"), event.unitPrice());
        assertEquals(new BigDecimal("494.10"), event.operationValue());
        assertEquals(new BigDecimal("0.00005"), event.aliquota());
        assertNotNull(event.operationDate());
    }

    @Test
    void deveCalcularIRComValorMaior() {
        List<Delivery> deliveries = List.of(
                Delivery.createDelivery(1L, 1L, "PETR4", 100, Money.create(new BigDecimal("50.00"))));

        Map<Long, CustodyPurchaseDataDTO> customers = createCustomersMap(1L, "12345678901");

        List<IRDedoDuroEvent> events = calculator.calculate(deliveries, customers);

        assertEquals(new BigDecimal("0.25"), events.get(0).valueIR());
    }

    private Map<Long, CustodyPurchaseDataDTO> createCustomersMap(Long accountId, String cpf) {
        Customer customerMock = mock(Customer.class);
        when(customerMock.getId()).thenReturn(1L);
        when(customerMock.getCpf()).thenReturn(cpf);

        BrokerageAccount accountMock = mock(BrokerageAccount.class);
        when(accountMock.getId()).thenReturn(accountId);
        when(accountMock.getCustomer()).thenReturn(customerMock);

        return Map.of(accountId, new CustodyPurchaseDataDTO(
                1L,
                "Test Customer",
                accountId,
                Money.create(BigDecimal.valueOf(333.33)),
                accountMock));
    }
}
