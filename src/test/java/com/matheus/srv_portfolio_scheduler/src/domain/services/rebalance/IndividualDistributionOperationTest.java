package com.matheus.srv_portfolio_scheduler.src.domain.services.rebalance;

import com.matheus.srv_portfolio_scheduler.domain.entities.BrokerageAccount;
import com.matheus.srv_portfolio_scheduler.domain.entities.Custody;
import com.matheus.srv_portfolio_scheduler.domain.entities.PurchaseOrder;
import com.matheus.srv_portfolio_scheduler.domain.enums.BrokerageAccountType;
import com.matheus.srv_portfolio_scheduler.domain.enums.MarketType;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.*;
import com.matheus.srv_portfolio_scheduler.domain.services.rebalance.IndividualDistributionOperation;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class IndividualDistributionOperationTest {

    private IndividualDistributionOperation operation;
    private BrokerageAccount masterAccount;

    @BeforeEach
    void setup() {
        operation = new IndividualDistributionOperation();

        List<Custody> masterCustodies = new ArrayList<>();
        masterCustodies.add(Custody.reconstruct(1L, null, "RENT3", 50,
                Money.create(new BigDecimal("50.00")), null));
        masterCustodies.add(Custody.reconstruct(2L, null, "ABEV3", 100,
                Money.create(new BigDecimal("14.00")), null));

        masterAccount = BrokerageAccount.reconstruct(
                1L, null, "MASTER", BrokerageAccountType.MASTER, null, masterCustodies);
    }

    @Test
    void deveDistribuirBaseadoNaNecessidadeIndividual() {
        List<PurchaseOrder> purchaseOrders = createPurchaseOrders();
        Map<Long, CustodyPurchaseDataDTO> customers = createCustomersMap();
        Map<Long, CustomerBuyNeedDTO> buyNeeds = createBuyNeeds();
        Map<String, TickerData> totalPerTicker = createTickerData();

        DistributionsResultDTO result = operation.distribute(
                purchaseOrders, customers, buyNeeds, masterAccount, totalPerTicker);

        assertFalse(result.deliveries().isEmpty());
        assertFalse(result.modifiedCustodies().isEmpty());
    }

    @Test
    void deveRetornarVazioQuandoClienteNaoTemNecessidade() {
        List<PurchaseOrder> purchaseOrders = createPurchaseOrders();
        Map<Long, CustodyPurchaseDataDTO> customers = createCustomersMap();
        Map<Long, CustomerBuyNeedDTO> buyNeeds = Map.of();
        Map<String, TickerData> totalPerTicker = createTickerData();

        DistributionsResultDTO result = operation.distribute(
                purchaseOrders, customers, buyNeeds, masterAccount, totalPerTicker);

        assertTrue(result.deliveries().isEmpty());
        assertTrue(result.distributions().isEmpty());
    }

    @Test
    void deveLimitarDistribuicaoAoDisponiveNaMaster() {
        List<Custody> masterCustodies = new ArrayList<>();
        masterCustodies.add(Custody.reconstruct(1L, null, "RENT3", 5,
                Money.create(new BigDecimal("50.00")), null));

        masterAccount = BrokerageAccount.reconstruct(
                1L, null, "MASTER", BrokerageAccountType.MASTER, null, masterCustodies);

        List<PurchaseOrder> purchaseOrders = List.of(
                PurchaseOrder.reconstruct(1L, 1L, "RENT3", 5,
                        Money.create(new BigDecimal("50.00")), MarketType.BATCH,
                        OffsetDateTime.now(), List.of()));

        Map<Long, CustodyPurchaseDataDTO> customers = createCustomersMap();

        Map<Long, CustomerBuyNeedDTO> buyNeeds = Map.of(
                1L, new CustomerBuyNeedDTO(1L, Map.of("RENT3", Money.create(new BigDecimal("500.00")))));

        Map<String, TickerData> totalPerTicker = Map.of(
                "RENT3", new TickerData(1L, 5, Money.create(new BigDecimal("50.00"))));

        DistributionsResultDTO result = operation.distribute(
                purchaseOrders, customers, buyNeeds, masterAccount, totalPerTicker);

        int totalDistributed = result.deliveries().stream()
                .mapToInt(d -> d.getQuantity())
                .sum();

        assertTrue(totalDistributed <= 5);
    }

    @Test
    void deveCriarCustodiaQuandoClienteNaoPossuiTicker() {
        List<PurchaseOrder> purchaseOrders = createPurchaseOrders();

        List<Custody> customerCustodies = new ArrayList<>();
        BrokerageAccount customerAccount = BrokerageAccount.reconstruct(
                1L, null, "CUSTOMER", BrokerageAccountType.CLIENT, null, customerCustodies);

        Map<Long, CustodyPurchaseDataDTO> customers = Map.of(
                1L, new CustodyPurchaseDataDTO(1L, "Customer 1", 1L,
                        Money.create(new BigDecimal("500.00")), customerAccount));

        Map<Long, CustomerBuyNeedDTO> buyNeeds = Map.of(
                1L, new CustomerBuyNeedDTO(1L, Map.of("RENT3", Money.create(new BigDecimal("150.00")))));

        Map<String, TickerData> totalPerTicker = createTickerData();

        DistributionsResultDTO result = operation.distribute(
                purchaseOrders, customers, buyNeeds, masterAccount, totalPerTicker);

        assertFalse(result.modifiedCustodies().isEmpty());

        boolean hasRent3Custody = result.modifiedCustodies().stream()
                .anyMatch(c -> c.getTicker().equals("RENT3"));
        assertTrue(hasRent3Custody);
    }

    @Test
    void deveCalcularQuantidadeCorretaBaseadoNoPreco() {
        List<PurchaseOrder> purchaseOrders = createPurchaseOrders();
        Map<Long, CustodyPurchaseDataDTO> customers = createCustomersMap();

        Map<Long, CustomerBuyNeedDTO> buyNeeds = Map.of(
                1L, new CustomerBuyNeedDTO(1L, Map.of("RENT3", Money.create(new BigDecimal("150.00")))));

        Map<String, TickerData> totalPerTicker = createTickerData();

        DistributionsResultDTO result = operation.distribute(
                purchaseOrders, customers, buyNeeds, masterAccount, totalPerTicker);

        int distributed = result.deliveries().stream()
                .filter(d -> d.getTicker().equals("RENT3"))
                .mapToInt(d -> d.getQuantity())
                .sum();

        assertEquals(3, distributed);
    }

    @Test
    void deveIgnorarTickerSemPrecoDisponivel() {
        List<PurchaseOrder> purchaseOrders = createPurchaseOrders();
        Map<Long, CustodyPurchaseDataDTO> customers = createCustomersMap();

        Map<Long, CustomerBuyNeedDTO> buyNeeds = Map.of(
                1L, new CustomerBuyNeedDTO(1L, Map.of("INEXISTENTE", Money.create(new BigDecimal("100.00")))));

        Map<String, TickerData> totalPerTicker = createTickerData();

        DistributionsResultDTO result = operation.distribute(
                purchaseOrders, customers, buyNeeds, masterAccount, totalPerTicker);

        boolean hasInexistente = result.deliveries().stream()
                .anyMatch(d -> d.getTicker().equals("INEXISTENTE"));
        assertFalse(hasInexistente);
    }

    private List<PurchaseOrder> createPurchaseOrders() {
        return List.of(
                PurchaseOrder.reconstruct(1L, 1L, "RENT3", 50,
                        Money.create(new BigDecimal("50.00")), MarketType.BATCH,
                        OffsetDateTime.now(), List.of()),
                PurchaseOrder.reconstruct(2L, 1L, "ABEV3", 100,
                        Money.create(new BigDecimal("14.00")), MarketType.BATCH,
                        OffsetDateTime.now(), List.of()));
    }

    private Map<Long, CustodyPurchaseDataDTO> createCustomersMap() {
        Map<Long, CustodyPurchaseDataDTO> customers = new LinkedHashMap<>();

        List<Custody> custodies = new ArrayList<>();
        custodies.add(Custody.reconstruct(10L, null, "RENT3", 0,
                Money.create(BigDecimal.ZERO), null));

        BrokerageAccount account = BrokerageAccount.reconstruct(
                1L, null, "CUSTOMER", BrokerageAccountType.CLIENT, null, custodies);

        customers.put(1L, new CustodyPurchaseDataDTO(1L, "Customer 1", 1L,
                Money.create(new BigDecimal("500.00")), account));

        return customers;
    }

    private Map<Long, CustomerBuyNeedDTO> createBuyNeeds() {
        return Map.of(
                1L, new CustomerBuyNeedDTO(1L, Map.of(
                        "RENT3", Money.create(new BigDecimal("150.00")),
                        "ABEV3", Money.create(new BigDecimal("100.00")))));
    }

    private Map<String, TickerData> createTickerData() {
        return Map.of(
                "RENT3", new TickerData(1L, 50, Money.create(new BigDecimal("50.00"))),
                "ABEV3", new TickerData(2L, 100, Money.create(new BigDecimal("14.00"))));
    }
}
