package com.matheus.srv_portfolio_scheduler.application.service;

import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.CustodyRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.CustomerRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.DedoDuroOutboxRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.DeliveryRepositoryPort;
import com.matheus.srv_portfolio_scheduler.domain.entities.*;
import com.matheus.srv_portfolio_scheduler.domain.enums.MarketType;
import com.matheus.srv_portfolio_scheduler.domain.events.IRDedoDuroEvent;
import com.matheus.srv_portfolio_scheduler.domain.services.IRDedoDuroCalculator;
import com.matheus.srv_portfolio_scheduler.domain.services.PortfolioDistribution;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.*;
import com.matheus.srv_portfolio_scheduler.domain.services.rebalance.IndividualDistributionOperation;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessDistributionInBatchTest {

    @Mock
    private CustodyRepositoryPort custodyRepository;

    @Mock
    private IRDedoDuroCalculator irDedoDuroCalculator;

    @Mock
    private CustomerRepositoryPort customerRepository;

    @Mock
    private DeliveryRepositoryPort deliveryRepository;

    @Mock
    private PortfolioDistribution portfolioDistribution;

    @Mock
    private IRDedoDuroOutboxService irDedoDuroOutboxService;

    @Mock
    private DedoDuroOutboxRepositoryPort dedoDuroOutboxRepository;

    @Mock
    private IndividualDistributionOperation individualDistributionOperation;

    @InjectMocks
    private ProcessDistributionInBatch service;

    private BrokerageAccount masterAccount;
    private List<PurchaseOrder> purchaseOrders;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(service, "BATCH_SIZE", 100);

        masterAccount = BrokerageAccount.reconstruct(1L, null, "MASTER", null, null, new ArrayList<>());

        purchaseOrders = List.of(
                createPurchaseOrder("PETR4", 30, "35.00"),
                createPurchaseOrder("VALE3", 14, "62.00"));
    }

    @Test
    void deveProcessarDistribuicaoEmLoteComSucesso() {
        Money thirdValue = Money.create(new BigDecimal("1166.67"));

        Map<Long, CustodyPurchaseDataDTO> customers = createCustomersMap();

        when(customerRepository.getChunkOfCustomers(anyLong(), anyInt()))
                .thenReturn(customers)
                .thenReturn(Map.of());

        when(portfolioDistribution.buildInitialTotalPerTicker(any(), any()))
                .thenReturn(Map.of());

        DistributionsResultDTO distributionResult = new DistributionsResultDTO(
                List.of(),
                List.of(),
                List.of(),
                List.of());

        when(portfolioDistribution.distribute(any(), any(), any(), any()))
                .thenReturn(distributionResult);

        when(irDedoDuroCalculator.calculate(any(), any()))
                .thenReturn(List.of());

        when(irDedoDuroOutboxService.createOutboxEntries(any()))
                .thenReturn(List.of());

        PurchaseSummaryDTO result = service.processInBatch(purchaseOrders, thirdValue, masterAccount);

        verify(customerRepository, atLeast(1)).getChunkOfCustomers(anyLong(), anyInt());
        verify(portfolioDistribution).distribute(any(), any(), any(), any());
        verify(custodyRepository, atLeast(1)).saveAll(any());

        assertEquals(3, result.totalCustomersProcessed());
    }

    @Test
    void deveAtualizarCustodiaMasterComQuantidadeComprada() {
        Custody existingCustody = Custody.reconstruct(
                1L, masterAccount, "PETR4", 2, Money.create(new BigDecimal("34.00")), null);
        masterAccount.getCustodies().add(existingCustody);

        service.updateMasterWithPurchaseQuantity(masterAccount, purchaseOrders);

        Custody petr4Custody = masterAccount.getCustodies().stream()
                .filter(c -> c.getTicker().equals("PETR4"))
                .findFirst().orElseThrow();

        assertEquals(32, petr4Custody.getQuantity());

        Custody vale3Custody = masterAccount.getCustodies().stream()
                .filter(c -> c.getTicker().equals("VALE3"))
                .findFirst().orElseThrow();

        assertEquals(14, vale3Custody.getQuantity());
    }

    @Test
    void deveCriarNovaCustodiaQuandoTickerNaoExisteNaMaster() {
        service.updateMasterWithPurchaseQuantity(masterAccount, purchaseOrders);

        assertEquals(2, masterAccount.getCustodies().size());

        assertTrue(masterAccount.getCustodies().stream()
                .anyMatch(c -> c.getTicker().equals("PETR4")));
        assertTrue(masterAccount.getCustodies().stream()
                .anyMatch(c -> c.getTicker().equals("VALE3")));
    }

    @Test
    void deveIgnorarPurchaseOrderComQuantidadeZero() {
        List<PurchaseOrder> ordersWithZero = List.of(
                createPurchaseOrder("PETR4", 0, "35.00"),
                createPurchaseOrder("VALE3", 14, "62.00"));

        service.updateMasterWithPurchaseQuantity(masterAccount, ordersWithZero);

        assertEquals(1, masterAccount.getCustodies().size());
        assertEquals("VALE3", masterAccount.getCustodies().get(0).getTicker());
    }

    @Test
    void deveProcessarDistribuicaoIndividualNoRebalanceamento() {
        Map<Long, CustodyPurchaseDataDTO> customers = createCustomersMap();

        RebalanceExecutionResultDTO rebalanceResult = new RebalanceExecutionResultDTO(
                Money.create(new BigDecimal("300.00")),
                List.of(new CustomerBuyNeedDTO(1L, Map.of("RENT3", Money.create(new BigDecimal("150.00"))))));

        when(customerRepository.getChunkOfCustomers(anyLong(), anyInt()))
                .thenReturn(customers)
                .thenReturn(Map.of());

        when(portfolioDistribution.buildInitialTotalPerTicker(any(), any()))
                .thenReturn(Map.of());

        DistributionsResultDTO distributionResult = new DistributionsResultDTO(
                List.of(),
                List.of(),
                List.of(),
                List.of());

        when(individualDistributionOperation.distribute(any(), any(), any(), any(), any()))
                .thenReturn(distributionResult);

        when(irDedoDuroCalculator.calculate(any(), any()))
                .thenReturn(List.of());

        when(irDedoDuroOutboxService.createOutboxEntries(any()))
                .thenReturn(List.of());

        PurchaseSummaryDTO result = service.processIndividualDistribution(
                purchaseOrders, rebalanceResult, masterAccount);

        verify(individualDistributionOperation).distribute(any(), any(), any(), any(), any());
        assertEquals(3, result.totalCustomersProcessed());
    }

    @Test
    void deveCalcularIRDedoDuroParaCadaEntrega() {
        Money thirdValue = Money.create(new BigDecimal("1166.67"));
        Map<Long, CustodyPurchaseDataDTO> customers = createCustomersMap();

        List<Delivery> deliveries = List.of(
                Delivery.createDelivery(1L, 1L, "PETR4", 8, Money.create(new BigDecimal("35.00"))));

        when(customerRepository.getChunkOfCustomers(anyLong(), anyInt()))
                .thenReturn(customers)
                .thenReturn(Map.of());

        when(portfolioDistribution.buildInitialTotalPerTicker(any(), any()))
                .thenReturn(Map.of());

        DistributionsResultDTO distributionResult = new DistributionsResultDTO(
                List.of(),
                List.of(),
                deliveries,
                List.of());

        when(portfolioDistribution.distribute(any(), any(), any(), any()))
                .thenReturn(distributionResult);

        List<IRDedoDuroEvent> irEvents = List.of();
        when(irDedoDuroCalculator.calculate(any(), any()))
                .thenReturn(irEvents);

        List<DedoDuroOutbox> outboxEntries = List.of(DedoDuroOutbox.create("payload"));
        when(irDedoDuroOutboxService.createOutboxEntries(any()))
                .thenReturn(outboxEntries);

        PurchaseSummaryDTO result = service.processInBatch(purchaseOrders, thirdValue, masterAccount);

        verify(irDedoDuroCalculator).calculate(eq(deliveries), eq(customers));
        verify(dedoDuroOutboxRepository).saveAll(outboxEntries);

        assertEquals(1, result.totalOutboxEntries());
    }

    private PurchaseOrder createPurchaseOrder(String ticker, int quantity, String price) {
        return PurchaseOrder.reconstruct(
                1L,
                1L,
                ticker,
                quantity,
                Money.create(new BigDecimal(price)),
                MarketType.BATCH,
                OffsetDateTime.now(),
                List.of());
    }

    private Map<Long, CustodyPurchaseDataDTO> createCustomersMap() {
        Map<Long, CustodyPurchaseDataDTO> customers = new LinkedHashMap<>();

        for (long i = 1; i <= 3; i++) {
            BrokerageAccount account = BrokerageAccount.reconstruct(
                    i, null, "CUSTOMER", null, null, new ArrayList<>());
            customers.put(i, new CustodyPurchaseDataDTO(
                    i,
                    "Customer " + i,
                    i,
                    Money.create(BigDecimal.valueOf(333.33 * i)),
                    account));
        }

        return customers;
    }
}
