package com.matheus.srv_portfolio_scheduler.application.service;

import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.BrokerageAccountRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.CustomerRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.PortfolioRebalanceRepositoryPort;
import com.matheus.srv_portfolio_scheduler.domain.entities.BrokerageAccount;
import com.matheus.srv_portfolio_scheduler.domain.entities.Customer;
import com.matheus.srv_portfolio_scheduler.domain.entities.Custody;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.*;
import com.matheus.srv_portfolio_scheduler.domain.services.rebalance.CalculateBuyNeedOperation;
import com.matheus.srv_portfolio_scheduler.domain.services.rebalance.CustodyTickerMigrationOperation;
import com.matheus.srv_portfolio_scheduler.domain.services.rebalance.SellExcessOperation;
import com.matheus.srv_portfolio_scheduler.domain.services.rebalance.SellRemovedAssetsOperation;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioRebalanceServiceTest {

    @Mock
    private SellExcessOperation sellExcessOperation;

    @Mock
    private CustomerRepositoryPort customerRepository;

    @Mock
    private BrokerageAccountRepositoryPort brokerageRepository;

    @Mock
    private CalculateBuyNeedOperation calculateBuyNeedOperation;

    @Mock
    private SellRemovedAssetsOperation sellRemovedAssetsOperation;

    @Mock
    private PortfolioRebalanceRepositoryPort portfolioRebalanceRepository;

    @Mock
    private CustodyTickerMigrationOperation custodyTickerMigrationOperation;

    @InjectMocks
    private PortfolioRebalanceService service;

    private BrokerageAccount masterAccount;
    private Customer masterCustomer;
    private Map<String, Money> pricesByTicker;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(service, "batch_size", 100);

        masterCustomer = mock(Customer.class);
        lenient().when(masterCustomer.getId()).thenReturn(1L);

        masterAccount = BrokerageAccount.reconstruct(1L, masterCustomer, "MASTER", null, null, new ArrayList<>());

        pricesByTicker = Map.of(
                "PETR4", Money.create(new BigDecimal("40.00")),
                "VALE3", Money.create(new BigDecimal("65.00")),
                "BBDC4", Money.create(new BigDecimal("15.00")),
                "WEGE3", Money.create(new BigDecimal("40.00")));
    }

    @Test
    void deveExecutarRebalanceamentoComSucesso() {
        PortfolioComparisonDTO comparison = createComparisonWithRemovedAssets();

        Map<Long, CustodyPurchaseDataDTO> customers = createCustomersMap();

        when(customerRepository.getChunkOfCustomers(anyLong(), anyInt()))
                .thenReturn(customers)
                .thenReturn(Map.of());

        RebalanceResultDTO removedResult = new RebalanceResultDTO(
                Money.create(new BigDecimal("150.00")), List.of());
        when(sellRemovedAssetsOperation.execute(any(), any(), any(), anyLong()))
                .thenReturn(removedResult);

        RebalanceResultDTO excessResult = new RebalanceResultDTO(
                Money.create(BigDecimal.ZERO), List.of());
        when(sellExcessOperation.execute(any(), any(), any(), anyLong(), any()))
                .thenReturn(excessResult);

        doNothing().when(custodyTickerMigrationOperation).execute(any(), any(), any());

        CustomerBuyNeedDTO buyNeed = new CustomerBuyNeedDTO(1L, Map.of());
        when(calculateBuyNeedOperation.execute(anyLong(), any(), any(), any(), any(), any()))
                .thenReturn(buyNeed);

        RebalanceExecutionResultDTO result = service.execute(masterAccount, comparison, pricesByTicker);

        verify(sellRemovedAssetsOperation, times(3)).execute(any(), any(), any(), anyLong());
        verify(sellExcessOperation, times(3)).execute(any(), any(), any(), anyLong(), any());
        verify(brokerageRepository, times(4)).save(any());

        assertTrue(result.totalReleasedAmount().getAmount().compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    void deveVenderPosicoesDasMasterQuandoAtivoForRemovido() {
        Custody bbdc4Custody = Custody.reconstruct(
                1L, masterAccount, "BBDC4", 50, Money.create(new BigDecimal("14.00")), null);
        masterAccount.getCustodies().add(bbdc4Custody);

        PortfolioComparisonDTO comparison = createComparisonWithRemovedAssets();

        when(customerRepository.getChunkOfCustomers(anyLong(), anyInt()))
                .thenReturn(Map.of());

        RebalanceExecutionResultDTO result = service.execute(masterAccount, comparison, pricesByTicker);

        assertEquals(0, bbdc4Custody.getQuantity());

        verify(portfolioRebalanceRepository).saveAndFlushAll(any());
    }

    @Test
    void deveCalcularNecessidadeDeCompraAposVendas() {
        PortfolioComparisonDTO comparison = createComparisonWithNewAssets();

        Map<Long, CustodyPurchaseDataDTO> customers = createCustomersMap();

        when(customerRepository.getChunkOfCustomers(anyLong(), anyInt()))
                .thenReturn(customers)
                .thenReturn(Map.of());

        RebalanceResultDTO removedResult = new RebalanceResultDTO(
                Money.create(new BigDecimal("200.00")), List.of());
        when(sellRemovedAssetsOperation.execute(any(), any(), any(), anyLong()))
                .thenReturn(removedResult);

        RebalanceResultDTO excessResult = new RebalanceResultDTO(
                Money.create(BigDecimal.ZERO), List.of());
        when(sellExcessOperation.execute(any(), any(), any(), anyLong(), any()))
                .thenReturn(excessResult);

        doNothing().when(custodyTickerMigrationOperation).execute(any(), any(), any());

        Map<String, Money> buyNeeds = Map.of(
                "RENT3", Money.create(new BigDecimal("100.00")),
                "ABEV3", Money.create(new BigDecimal("100.00")));
        CustomerBuyNeedDTO buyNeed = new CustomerBuyNeedDTO(1L, buyNeeds);
        when(calculateBuyNeedOperation.execute(anyLong(), any(), any(), any(), any(), any()))
                .thenReturn(buyNeed);

        RebalanceExecutionResultDTO result = service.execute(masterAccount, comparison, pricesByTicker);

        verify(calculateBuyNeedOperation, times(3)).execute(anyLong(), any(), any(), any(), any(), any());

        assertFalse(result.customerBuyNeeds().isEmpty());
    }

    @Test
    void deveProcessarClientesEmLotes() {
        ReflectionTestUtils.setField(service, "batch_size", 2);

        PortfolioComparisonDTO comparison = createComparisonWithRemovedAssets();

        Map<Long, CustodyPurchaseDataDTO> batch1 = new LinkedHashMap<>();
        batch1.put(1L, createCustomerData(1L));
        batch1.put(2L, createCustomerData(2L));

        Map<Long, CustodyPurchaseDataDTO> batch2 = new LinkedHashMap<>();
        batch2.put(3L, createCustomerData(3L));

        when(customerRepository.getChunkOfCustomers(anyLong(), eq(2)))
                .thenReturn(batch1)
                .thenReturn(batch2)
                .thenReturn(Map.of());

        RebalanceResultDTO result = new RebalanceResultDTO(
                Money.create(BigDecimal.ZERO), List.of());
        when(sellRemovedAssetsOperation.execute(any(), any(), any(), anyLong()))
                .thenReturn(result);
        when(sellExcessOperation.execute(any(), any(), any(), anyLong(), any()))
                .thenReturn(result);

        doNothing().when(custodyTickerMigrationOperation).execute(any(), any(), any());

        when(calculateBuyNeedOperation.execute(anyLong(), any(), any(), any(), any(), any()))
                .thenReturn(new CustomerBuyNeedDTO(1L, Map.of()));

        service.execute(masterAccount, comparison, pricesByTicker);

        verify(customerRepository, atLeast(2)).getChunkOfCustomers(anyLong(), eq(2));
        verify(sellRemovedAssetsOperation, times(3)).execute(any(), any(), any(), anyLong());
    }

    private PortfolioComparisonDTO createComparisonWithRemovedAssets() {
        return new PortfolioComparisonDTO(
                List.of(),
                List.of(
                        new PortfolioComparisonDTO.RemovedItem("BBDC4"),
                        new PortfolioComparisonDTO.RemovedItem("WEGE3")),
                List.of());
    }

    private PortfolioComparisonDTO createComparisonWithNewAssets() {
        return new PortfolioComparisonDTO(
                List.of(),
                List.of(new PortfolioComparisonDTO.RemovedItem("BBDC4")),
                List.of(
                        new PortfolioComparisonDTO.NewItem("RENT3", new BigDecimal("15")),
                        new PortfolioComparisonDTO.NewItem("ABEV3", new BigDecimal("20"))));
    }

    private Map<Long, CustodyPurchaseDataDTO> createCustomersMap() {
        Map<Long, CustodyPurchaseDataDTO> customers = new LinkedHashMap<>();
        for (long i = 1; i <= 3; i++) {
            customers.put(i, createCustomerData(i));
        }
        return customers;
    }

    private CustodyPurchaseDataDTO createCustomerData(Long id) {
        List<Custody> custodies = new ArrayList<>();
        custodies.add(Custody.reconstruct(
                id, null, "PETR4", 8, Money.create(new BigDecimal("35.00")), null));
        custodies.add(Custody.reconstruct(
                id + 10, null, "BBDC4", 10, Money.create(new BigDecimal("14.00")), null));

        BrokerageAccount account = BrokerageAccount.reconstruct(
                id, null, "CUSTOMER", null, null, custodies);

        return new CustodyPurchaseDataDTO(
                id,
                "Customer " + id,
                id,
                Money.create(BigDecimal.valueOf(1000.00)),
                account);
    }
}
