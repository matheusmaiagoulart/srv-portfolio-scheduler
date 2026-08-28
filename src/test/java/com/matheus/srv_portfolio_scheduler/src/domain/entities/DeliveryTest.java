package com.matheus.srv_portfolio_scheduler.src.domain.entities;

import com.matheus.srv_portfolio_scheduler.domain.entities.Delivery;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DeliveryTest {

    @Test
    void deveCriarEntrega() {
        Delivery delivery = Delivery.createDelivery(
                1L,
                100L,
                "PETR4",
                50,
                Money.create(new BigDecimal("35.00")));

        assertEquals(1L, delivery.getPurchaseOrderId());
        assertEquals(100L, delivery.getCustodyCustomerId());
        assertEquals("PETR4", delivery.getTicker());
        assertEquals(50, delivery.getQuantity());
        assertEquals(0, delivery.getUnitPrice().getAmount().compareTo(new BigDecimal("35.00")));
        assertNotNull(delivery.getDeliveryDate());
    }

    @Test
    void deveDefinirDataDeEntregaAutomaticamente() {
        LocalDateTime antes = LocalDateTime.now().minusSeconds(1);

        Delivery delivery = Delivery.createDelivery(1L, 100L, "VALE3", 30,
                Money.create(new BigDecimal("62.00")));

        LocalDateTime depois = LocalDateTime.now().plusSeconds(1);

        assertTrue(delivery.getDeliveryDate().isAfter(antes));
        assertTrue(delivery.getDeliveryDate().isBefore(depois));
    }

    @Test
    void deveReconstruirEntrega() {
        LocalDateTime dataEntrega = LocalDateTime.of(2026, 8, 15, 10, 30);

        Delivery delivery = Delivery.reconstruct(
                1L,
                10L,
                200L,
                "ITUB4",
                75,
                Money.create(new BigDecimal("25.00")),
                dataEntrega);

        assertEquals(1L, delivery.getId());
        assertEquals(10L, delivery.getPurchaseOrderId());
        assertEquals(200L, delivery.getCustodyCustomerId());
        assertEquals("ITUB4", delivery.getTicker());
        assertEquals(75, delivery.getQuantity());
        assertEquals(dataEntrega, delivery.getDeliveryDate());
    }

    @Test
    void deveCriarEntregaComQuantidadePequena() {
        Delivery delivery = Delivery.createDelivery(1L, 100L, "WEGE3", 1,
                Money.create(new BigDecimal("40.00")));

        assertEquals(1, delivery.getQuantity());
    }

    @Test
    void deveCriarEntregaComQuantidadeGrande() {
        Delivery delivery = Delivery.createDelivery(1L, 100L, "BBDC4", 10000,
                Money.create(new BigDecimal("15.00")));

        assertEquals(10000, delivery.getQuantity());
    }

    @Test
    void deveManterPrecisaoDoPreco() {
        Delivery delivery = Delivery.createDelivery(1L, 100L, "ABEV3", 50,
                Money.create(new BigDecimal("14.57")));

        assertEquals(0, delivery.getUnitPrice().getAmount().compareTo(new BigDecimal("14.57")));
    }
}
