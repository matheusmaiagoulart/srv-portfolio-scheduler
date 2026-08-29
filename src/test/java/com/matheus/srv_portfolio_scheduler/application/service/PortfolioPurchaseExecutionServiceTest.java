package com.matheus.srv_portfolio_scheduler.application.service;

import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.PurchaseOrderRepositoryPort;
import com.matheus.srv_portfolio_scheduler.domain.entities.*;
import com.matheus.srv_portfolio_scheduler.domain.services.PurchaseQuotesCalculator;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.*;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioPurchaseExecutionServiceTest {

    @Mock
    private ProcessDistributionInBatch distributionInBatch;

    @Mock
    private PurchaseQuotesCalculator purchaseQuotesCalculator;

    @Mock
    private PurchaseOrderRepositoryPort purchaseOrderRepository;

    @InjectMocks
    private PortfolioPurchaseExecutionService service;

    private RecommendedPortfolio portfolio;
    private BrokerageAccount masterAccount;
    private List<QuoteDTO> quotes;

    @BeforeEach
    void setup() {
        List<PortfolioItem> items = List.of(
                PortfolioItem.create("PETR4", 30),
                PortfolioItem.create("VALE3", 25),
                PortfolioItem.create("ITUB4", 20),
                PortfolioItem.create("BBDC4", 15),
                PortfolioItem.create("WEGE3", 10));
        portfolio = RecommendedPortfolio.create("Test Portfolio", items, null);

        masterAccount = BrokerageAccount.reconstruct(1L, null, "MASTER", null, null, new ArrayList<>());

        quotes = List.of(
                createQuote("PETR4", "35.00"),
                createQuote("PETR4F", "35.00"),
                createQuote("VALE3", "62.00"),
                createQuote("VALE3F", "62.00"));
    }

    @Test
    void deveExecutarCompraComSucesso() {
        Money totalAmount = Money.create(new BigDecimal("3500.00"));

        Map<String, Money> amountPerAsset = Map.of(
                "PETR4", Money.create(new BigDecimal("1050.00")),
                "VALE3", Money.create(new BigDecimal("875.00")));

        Map<String, AssetPurchaseDTO> quantityPerAsset = Map.of(
                "PETR4", createAssetPurchase("PETR4", 30, "35.00"),
                "VALE3", createAssetPurchase("VALE3", 14, "62.00"));

        when(purchaseQuotesCalculator.calculateAmountPerAsset(totalAmount, portfolio))
                .thenReturn(amountPerAsset);
        when(purchaseQuotesCalculator.calculateQuantityPerAsset(any(), any(), any()))
                .thenReturn(quantityPerAsset);
        when(purchaseOrderRepository.save(anyList())).thenAnswer(i -> i.getArgument(0));
        when(distributionInBatch.processInBatch(any(), any(), any()))
                .thenReturn(new PurchaseSummaryDTO(List.of(), List.of(), 10, 20, 20));

        PurchaseSummaryDTO result = service.executePurchase(portfolio, masterAccount, totalAmount, quotes);

        verify(purchaseQuotesCalculator).calculateAmountPerAsset(totalAmount, portfolio);
        verify(purchaseQuotesCalculator).calculateQuantityPerAsset(any(), any(), any());
        verify(purchaseOrderRepository).save(anyList());
        verify(distributionInBatch).processInBatch(any(), eq(totalAmount), eq(masterAccount));

        assertEquals(10, result.totalCustomersProcessed());
        assertEquals(20, result.totalDeliveries());
    }

    @Test
    void deveExecutarCompraDeRebalanceamentoComNecessidadeDeCompra() {
        Map<String, Money> aggregatedNeed = Map.of(
                "RENT3", Money.create(new BigDecimal("150.00")),
                "ABEV3", Money.create(new BigDecimal("100.00")));

        RebalanceExecutionResultDTO rebalanceResult = new RebalanceExecutionResultDTO(
                Money.create(new BigDecimal("300.00")),
                List.of(new CustomerBuyNeedDTO(1L, aggregatedNeed)));

        Map<String, AssetPurchaseDTO> quantityPerAsset = Map.of(
                "RENT3", createAssetPurchase("RENT3", 3, "50.00"),
                "ABEV3", createAssetPurchase("ABEV3", 7, "14.00"));

        when(purchaseQuotesCalculator.calculateQuantityPerAsset(any(), any(), any()))
                .thenReturn(quantityPerAsset);
        when(purchaseOrderRepository.save(anyList())).thenAnswer(i -> i.getArgument(0));
        when(distributionInBatch.processIndividualDistribution(any(), any(), any()))
                .thenReturn(new PurchaseSummaryDTO(List.of(), List.of(), 5, 10, 10));

        PurchaseSummaryDTO result = service.executePurchase(portfolio, masterAccount, rebalanceResult, quotes);

        verify(purchaseQuotesCalculator).calculateQuantityPerAsset(any(), any(), any());
        verify(purchaseOrderRepository).save(anyList());
        verify(distributionInBatch).processIndividualDistribution(any(), any(), any());

        assertEquals(5, result.totalCustomersProcessed());
    }

    @Test
    void deveRetornarVazioQuandoNaoHouverNecessidadeDeCompraNoRebalanceamento() {
        RebalanceExecutionResultDTO rebalanceResult = new RebalanceExecutionResultDTO(
                Money.create(BigDecimal.ZERO),
                List.of());

        PurchaseSummaryDTO result = service.executePurchase(portfolio, masterAccount, rebalanceResult, quotes);

        verify(purchaseQuotesCalculator, never()).calculateQuantityPerAsset(any(), any(), any());
        verify(purchaseOrderRepository, never()).save(anyList());
        verify(distributionInBatch, never()).processIndividualDistribution(any(), any(), any());

        assertEquals(0, result.totalCustomersProcessed());
        assertEquals(0, result.totalDeliveries());
        assertTrue(result.purchaseOrdersPerAssets().isEmpty());
    }

    private QuoteDTO createQuote(String ticker, String closePrice) {
        return new QuoteDTO(
                LocalDate.now(),
                ticker,
                "02",
                10,
                "Test Company",
                new BigDecimal(closePrice),
                new BigDecimal(closePrice),
                new BigDecimal(closePrice),
                new BigDecimal(closePrice),
                new BigDecimal(closePrice),
                1000L,
                new BigDecimal("100000.00"));
    }

    private AssetPurchaseDTO createAssetPurchase(String ticker, int quantity, String price) {
        Money priceValue = Money.create(new BigDecimal(price));
        return new AssetPurchaseDTO(
                ticker,
                priceValue,
                priceValue,
                quantity,
                0,
                quantity,
                new AssetPurchaseDTO.MarketTypePurchase(0, quantity));
    }
}
