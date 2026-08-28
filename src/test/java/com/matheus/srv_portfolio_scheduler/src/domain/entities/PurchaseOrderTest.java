package com.matheus.srv_portfolio_scheduler.src.domain.entities;

import com.matheus.srv_portfolio_scheduler.domain.entities.PurchaseOrder;
import com.matheus.srv_portfolio_scheduler.domain.enums.MarketType;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.AssetPurchaseDTO;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PurchaseOrderTest {

    @Test
    void deveCriarOrdemDeCompraParaLote() {
        AssetPurchaseDTO asset = new AssetPurchaseDTO(
                "PETR4",
                Money.create(new BigDecimal("35.00")),
                Money.create(new BigDecimal("35.00")),
                100,
                0,
                100,
                new AssetPurchaseDTO.MarketTypePurchase(1, 0));

        List<PurchaseOrder> orders = PurchaseOrder.createPurchaseOrders(1L, List.of(asset));

        assertEquals(1, orders.size());
        assertEquals("PETR4", orders.get(0).getTicker());
        assertEquals(100, orders.get(0).getQuantity());
        assertEquals(MarketType.BATCH, orders.get(0).getMarketType());
    }

    @Test
    void deveCriarOrdemParaLoteEFracionario() {
        AssetPurchaseDTO asset = new AssetPurchaseDTO(
                "VALE3",
                Money.create(new BigDecimal("62.00")),
                Money.create(new BigDecimal("62.00")),
                130,
                0,
                130,
                new AssetPurchaseDTO.MarketTypePurchase(1, 30));

        List<PurchaseOrder> orders = PurchaseOrder.createPurchaseOrders(1L, List.of(asset));

        assertEquals(2, orders.size());

        PurchaseOrder batchOrder = orders.stream()
                .filter(o -> o.getMarketType() == MarketType.BATCH)
                .findFirst().orElseThrow();
        assertEquals(100, batchOrder.getQuantity());

        PurchaseOrder fractionalOrder = orders.stream()
                .filter(o -> o.getMarketType() == MarketType.FRACTIONAL)
                .findFirst().orElseThrow();
        assertEquals(30, fractionalOrder.getQuantity());
    }

    @Test
    void deveRetornarListaVaziaQuandoListaDeAssetsNula() {
        List<PurchaseOrder> orders = PurchaseOrder.createPurchaseOrders(1L, null);

        assertTrue(orders.isEmpty());
    }

    @Test
    void deveRetornarListaVaziaQuandoListaDeAssetsVazia() {
        List<PurchaseOrder> orders = PurchaseOrder.createPurchaseOrders(1L, List.of());

        assertTrue(orders.isEmpty());
    }

    @Test
    void deveCriarMultiplasOrdensParaMultiplosAtivos() {
        List<AssetPurchaseDTO> assets = List.of(
                new AssetPurchaseDTO("PETR4",
                        Money.create(new BigDecimal("35.00")),
                        Money.create(new BigDecimal("35.00")),
                        100, 0, 100,
                        new AssetPurchaseDTO.MarketTypePurchase(1, 0)),
                new AssetPurchaseDTO("VALE3",
                        Money.create(new BigDecimal("62.00")),
                        Money.create(new BigDecimal("62.00")),
                        200, 0, 200,
                        new AssetPurchaseDTO.MarketTypePurchase(2, 0)));

        List<PurchaseOrder> orders = PurchaseOrder.createPurchaseOrders(1L, assets);

        assertEquals(2, orders.size());
        assertTrue(orders.stream().anyMatch(o -> o.getTicker().equals("PETR4")));
        assertTrue(orders.stream().anyMatch(o -> o.getTicker().equals("VALE3")));
    }

    @Test
    void deveReconstruirOrdemDeCompra() {
        PurchaseOrder order = PurchaseOrder.reconstruct(
                1L,
                10L,
                "ITUB4",
                150,
                Money.create(new BigDecimal("25.00")),
                MarketType.BATCH,
                OffsetDateTime.now(),
                List.of());

        assertEquals(1L, order.getId());
        assertEquals(10L, order.getMasterAccountId());
        assertEquals("ITUB4", order.getTicker());
        assertEquals(150, order.getQuantity());
        assertEquals(MarketType.BATCH, order.getMarketType());
    }

    @Test
    void deveCalcularQuantidadeCorretaParaLote() {
        AssetPurchaseDTO asset = new AssetPurchaseDTO(
                "WEGE3",
                Money.create(new BigDecimal("40.00")),
                Money.create(new BigDecimal("40.00")),
                300,
                0,
                300,
                new AssetPurchaseDTO.MarketTypePurchase(3, 0));

        List<PurchaseOrder> orders = PurchaseOrder.createPurchaseOrders(1L, List.of(asset));

        assertEquals(1, orders.size());
        assertEquals(300, orders.get(0).getQuantity());
    }

    @Test
    void deveDefinirMasterAccountIdCorretamente() {
        AssetPurchaseDTO asset = new AssetPurchaseDTO(
                "BBDC4",
                Money.create(new BigDecimal("15.00")),
                Money.create(new BigDecimal("15.00")),
                100, 0, 100,
                new AssetPurchaseDTO.MarketTypePurchase(1, 0));

        List<PurchaseOrder> orders = PurchaseOrder.createPurchaseOrders(99L, List.of(asset));

        assertEquals(99L, orders.get(0).getMasterAccountId());
    }
}
