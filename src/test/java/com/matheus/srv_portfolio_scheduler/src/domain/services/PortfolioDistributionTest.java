package com.matheus.srv_portfolio_scheduler.src.domain.services;

import com.matheus.srv_portfolio_scheduler.domain.entities.BrokerageAccount;
import com.matheus.srv_portfolio_scheduler.domain.entities.Custody;
import com.matheus.srv_portfolio_scheduler.domain.entities.PurchaseOrder;
import com.matheus.srv_portfolio_scheduler.domain.enums.BrokerageAccountType;
import com.matheus.srv_portfolio_scheduler.domain.enums.MarketType;
import com.matheus.srv_portfolio_scheduler.domain.services.PortfolioDistribution;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.*;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PortfolioDistributionTest {

    private PortfolioDistribution portfolioDistribution;
    private BrokerageAccount masterAccount;

    @BeforeEach
    void setup() {
        portfolioDistribution = new PortfolioDistribution();

        List<Custody> masterCustodies = new ArrayList<>();
        masterCustodies.add(Custody.reconstruct(1L, null, "PETR4", 100,
                Money.create(new BigDecimal("35.00")), null));
        masterCustodies.add(Custody.reconstruct(2L, null, "VALE3", 50,
                Money.create(new BigDecimal("62.00")), null));

        masterAccount = BrokerageAccount.reconstruct(
                1L, null, "MASTER", BrokerageAccountType.MASTER, null, masterCustodies);
    }

    @Test
    void deveDistribuirAtivosProporcionalmente() {
        List<PurchaseOrder> purchaseOrders = createPurchaseOrders();
        Map<String, TickerData> totalPerTicker = portfolioDistribution.buildInitialTotalPerTicker(purchaseOrders, masterAccount);
        Map<Long, CustodyPurchaseDataDTO> customers = createCustomersMap();

        Money totalAmount = Money.create(new BigDecimal("3500.00"));
        PurchaseRoundDataDTO purchaseRoundData = new PurchaseRoundDataDTO(totalAmount, customers);

        DistributionsResultDTO result = portfolioDistribution.distribute(
                purchaseOrders, purchaseRoundData, masterAccount, totalPerTicker);

        assertFalse(result.deliveries().isEmpty());
        assertFalse(result.distributions().isEmpty());
        assertEquals(2, result.purchaseOrdersPerAssets().size());
    }

    @Test
    void deveRetornarDistribuicaoVaziaQuandoValorTotalZero() {
        List<PurchaseOrder> purchaseOrders = createPurchaseOrders();
        Map<String, TickerData> totalPerTicker = portfolioDistribution.buildInitialTotalPerTicker(purchaseOrders, masterAccount);
        Map<Long, CustodyPurchaseDataDTO> customers = createCustomersMap();

        Money totalAmount = Money.create(BigDecimal.ZERO);
        PurchaseRoundDataDTO purchaseRoundData = new PurchaseRoundDataDTO(totalAmount, customers);

        DistributionsResultDTO result = portfolioDistribution.distribute(
                purchaseOrders, purchaseRoundData, masterAccount, totalPerTicker);

        assertTrue(result.deliveries().isEmpty());
        assertTrue(result.distributions().isEmpty());
        assertFalse(result.purchaseOrdersPerAssets().isEmpty());
    }

    @Test
    void deveConstruirTotalPorTickerCorretamente() {
        List<PurchaseOrder> purchaseOrders = createPurchaseOrders();

        Map<String, TickerData> result = portfolioDistribution.buildInitialTotalPerTicker(purchaseOrders, masterAccount);

        assertTrue(result.containsKey("PETR4"));
        assertTrue(result.containsKey("VALE3"));
        assertEquals(100, result.get("PETR4").totalQuantity());
        assertEquals(50, result.get("VALE3").totalQuantity());
    }

    @Test
    void deveSubtrairQuantidadeDaMasterAposDistribuicao() {
        List<PurchaseOrder> purchaseOrders = createPurchaseOrders();
        Map<String, TickerData> totalPerTicker = portfolioDistribution.buildInitialTotalPerTicker(purchaseOrders, masterAccount);
        Map<Long, CustodyPurchaseDataDTO> customers = createCustomersMap();

        Money totalAmount = Money.create(new BigDecimal("3500.00"));
        PurchaseRoundDataDTO purchaseRoundData = new PurchaseRoundDataDTO(totalAmount, customers);

        int initialPetr4 = masterAccount.getCustodies().stream()
                .filter(c -> c.getTicker().equals("PETR4"))
                .findFirst().get().getQuantity();

        portfolioDistribution.distribute(purchaseOrders, purchaseRoundData, masterAccount, totalPerTicker);

        int finalPetr4 = masterAccount.getCustodies().stream()
                .filter(c -> c.getTicker().equals("PETR4"))
                .findFirst().get().getQuantity();

        assertTrue(finalPetr4 <= initialPetr4);
    }

    @Test
    void deveDistribuirMaisParaClienteComMaiorSaldo() {
        List<PurchaseOrder> purchaseOrders = createPurchaseOrders();
        Map<String, TickerData> totalPerTicker = portfolioDistribution.buildInitialTotalPerTicker(purchaseOrders, masterAccount);

        Map<Long, CustodyPurchaseDataDTO> customers = new LinkedHashMap<>();
        customers.put(1L, createCustomerData(1L, "1000.00"));
        customers.put(2L, createCustomerData(2L, "2500.00"));

        Money totalAmount = Money.create(new BigDecimal("3500.00"));
        PurchaseRoundDataDTO purchaseRoundData = new PurchaseRoundDataDTO(totalAmount, customers);

        DistributionsResultDTO result = portfolioDistribution.distribute(
                purchaseOrders, purchaseRoundData, masterAccount, totalPerTicker);

        int deliveriesCustomer1 = (int) result.deliveries().stream()
                .filter(d -> d.getCustodyCustomerId() == 1L)
                .mapToInt(d -> d.getQuantity())
                .sum();

        int deliveriesCustomer2 = (int) result.deliveries().stream()
                .filter(d -> d.getCustodyCustomerId() == 2L)
                .mapToInt(d -> d.getQuantity())
                .sum();

        assertTrue(deliveriesCustomer2 > deliveriesCustomer1);
    }

    @Test
    void naoDeveDistribuirQuantidadeNegativa() {
        List<PurchaseOrder> purchaseOrders = createPurchaseOrders();
        Map<String, TickerData> totalPerTicker = portfolioDistribution.buildInitialTotalPerTicker(purchaseOrders, masterAccount);
        Map<Long, CustodyPurchaseDataDTO> customers = createCustomersMap();

        Money totalAmount = Money.create(new BigDecimal("3500.00"));
        PurchaseRoundDataDTO purchaseRoundData = new PurchaseRoundDataDTO(totalAmount, customers);

        DistributionsResultDTO result = portfolioDistribution.distribute(
                purchaseOrders, purchaseRoundData, masterAccount, totalPerTicker);

        boolean hasNegativeQuantity = result.deliveries().stream()
                .anyMatch(d -> d.getQuantity() < 0);

        assertFalse(hasNegativeQuantity);
    }

    @Test
    void deveAtualizarCustodiaDoClienteAposDistribuicao() {
        List<PurchaseOrder> purchaseOrders = createPurchaseOrders();
        Map<String, TickerData> totalPerTicker = portfolioDistribution.buildInitialTotalPerTicker(purchaseOrders, masterAccount);
        Map<Long, CustodyPurchaseDataDTO> customers = createCustomersMap();

        Money totalAmount = Money.create(new BigDecimal("3500.00"));
        PurchaseRoundDataDTO purchaseRoundData = new PurchaseRoundDataDTO(totalAmount, customers);

        DistributionsResultDTO result = portfolioDistribution.distribute(
                purchaseOrders, purchaseRoundData, masterAccount, totalPerTicker);

        assertFalse(result.modifiedCustodies().isEmpty());
    }

    @Test
    void deveIgnorarClienteComSaldoInsuficiente() {
        List<PurchaseOrder> purchaseOrders = createPurchaseOrders();
        Map<String, TickerData> totalPerTicker = portfolioDistribution.buildInitialTotalPerTicker(purchaseOrders, masterAccount);

        Map<Long, CustodyPurchaseDataDTO> customers = new LinkedHashMap<>();
        customers.put(1L, createCustomerData(1L, "0.01"));

        Money totalAmount = Money.create(new BigDecimal("3500.00"));
        PurchaseRoundDataDTO purchaseRoundData = new PurchaseRoundDataDTO(totalAmount, customers);

        DistributionsResultDTO result = portfolioDistribution.distribute(
                purchaseOrders, purchaseRoundData, masterAccount, totalPerTicker);

        boolean hasDeliveriesForCustomer1 = result.deliveries().stream()
                .anyMatch(d -> d.getCustodyCustomerId() == 1L);

        assertFalse(hasDeliveriesForCustomer1);
    }

    private List<PurchaseOrder> createPurchaseOrders() {
        return List.of(
                PurchaseOrder.reconstruct(1L, 1L, "PETR4", 100,
                        Money.create(new BigDecimal("35.00")), MarketType.BATCH,
                        OffsetDateTime.now(), List.of()),
                PurchaseOrder.reconstruct(2L, 1L, "VALE3", 50,
                        Money.create(new BigDecimal("62.00")), MarketType.BATCH,
                        OffsetDateTime.now(), List.of()));
    }

    private Map<Long, CustodyPurchaseDataDTO> createCustomersMap() {
        Map<Long, CustodyPurchaseDataDTO> customers = new LinkedHashMap<>();
        customers.put(1L, createCustomerData(1L, "1166.67"));
        customers.put(2L, createCustomerData(2L, "1166.67"));
        customers.put(3L, createCustomerData(3L, "1166.66"));
        return customers;
    }

    private CustodyPurchaseDataDTO createCustomerData(Long id, String balance) {
        List<Custody> custodies = new ArrayList<>();
        custodies.add(Custody.reconstruct(id * 10, null, "PETR4", 0,
                Money.create(BigDecimal.ZERO), null));
        custodies.add(Custody.reconstruct(id * 10 + 1, null, "VALE3", 0,
                Money.create(BigDecimal.ZERO), null));

        BrokerageAccount account = BrokerageAccount.reconstruct(
                id, null, "CUSTOMER", BrokerageAccountType.CLIENT, null, custodies);

        return new CustodyPurchaseDataDTO(
                id,
                "Customer " + id,
                id,
                Money.create(new BigDecimal(balance)),
                account);
    }
}
