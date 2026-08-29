package com.matheus.srv_portfolio_scheduler.src.domain.services;

import com.matheus.srv_portfolio_scheduler.domain.entities.BrokerageAccount;
import com.matheus.srv_portfolio_scheduler.domain.entities.Custody;
import com.matheus.srv_portfolio_scheduler.domain.entities.PortfolioItem;
import com.matheus.srv_portfolio_scheduler.domain.entities.RecommendedPortfolio;
import com.matheus.srv_portfolio_scheduler.domain.exceptions.QuoteNotFoundException;
import com.matheus.srv_portfolio_scheduler.domain.services.PurchaseQuotesCalculator;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.AssetPurchaseDTO;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.QuoteDTO;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PurchaseQuotesCalculatorTest {

    private final PurchaseQuotesCalculator calculator = new PurchaseQuotesCalculator();

    @Test
    void deveCalcularValorPorAtivoBaseadoNoPercentual() {
        Money totalAmount = Money.create(new BigDecimal("3500.00"));
        RecommendedPortfolio portfolio = createPortfolio();

        Map<String, Money> result = calculator.calculateAmountPerAsset(totalAmount, portfolio);

        assertEquals(new BigDecimal("1050.00"), result.get("PETR4").getAmount());
        assertEquals(new BigDecimal("875.00"), result.get("VALE3").getAmount());
        assertEquals(new BigDecimal("700.00"), result.get("ITUB4").getAmount());
        assertEquals(new BigDecimal("525.00"), result.get("BBDC4").getAmount());
        assertEquals(new BigDecimal("350.00"), result.get("WEGE3").getAmount());
    }

    @Test
    void deveCalcularQuantidadeTruncandoParaBaixo() {
        Map<String, Money> amountPerAsset = Map.of(
                "PETR4", Money.create(new BigDecimal("1050.00")),
                "VALE3", Money.create(new BigDecimal("875.00")));

        List<QuoteDTO> quotes = List.of(
                createQuote("PETR4", "35.00"),
                createQuote("PETR4F", "35.00"),
                createQuote("VALE3", "62.00"),
                createQuote("VALE3F", "62.00"));

        BrokerageAccount masterAccount = createMasterAccountWithoutCustodies();

        Map<String, AssetPurchaseDTO> result = calculator.calculateQuantityPerAsset(amountPerAsset, quotes, masterAccount);

        assertEquals(30, result.get("PETR4").quantityToBuy());
        assertEquals(14, result.get("VALE3").quantityToBuy());
    }

    @Test
    void deveDescontarSaldoDaCustodiaMaster() {
        Map<String, Money> amountPerAsset = Map.of(
                "PETR4", Money.create(new BigDecimal("1050.00")),
                "VALE3", Money.create(new BigDecimal("875.00")));

        List<QuoteDTO> quotes = List.of(
                createQuote("PETR4", "35.00"),
                createQuote("PETR4F", "35.00"),
                createQuote("VALE3", "62.00"),
                createQuote("VALE3F", "62.00"));

        BrokerageAccount masterAccount = createMasterAccountWithCustodies();

        Map<String, AssetPurchaseDTO> result = calculator.calculateQuantityPerAsset(amountPerAsset, quotes, masterAccount);

        assertEquals(28, result.get("PETR4").quantityToBuy());
        assertEquals(2, result.get("PETR4").quantityFromMasterAccount());
        assertEquals(14, result.get("VALE3").quantityToBuy());
        assertEquals(0, result.get("VALE3").quantityFromMasterAccount());
    }

    @Test
    void deveSepararLotePadraoEFracionario() {
        Map<String, Money> amountPerAsset = Map.of(
                "PETR4", Money.create(new BigDecimal("12250.00")));

        List<QuoteDTO> quotes = List.of(
                createQuote("PETR4", "35.00"),
                createQuote("PETR4F", "35.00"));

        BrokerageAccount masterAccount = createMasterAccountWithoutCustodies();

        Map<String, AssetPurchaseDTO> result = calculator.calculateQuantityPerAsset(amountPerAsset, quotes, masterAccount);

        assertEquals(350, result.get("PETR4").quantityToBuy());
        assertEquals(3, result.get("PETR4").marketType().loteQuantity());
        assertEquals(50, result.get("PETR4").marketType().fractionalQuantity());
    }

    @Test
    void deveLancarExcecaoQuandoCotacaoNaoEncontrada() {
        Map<String, Money> amountPerAsset = Map.of(
                "PETR4", Money.create(new BigDecimal("1000.00")));

        List<QuoteDTO> quotes = List.of();

        BrokerageAccount masterAccount = createMasterAccountWithoutCustodies();

        assertThrows(NullPointerException.class,
                () -> calculator.calculateQuantityPerAsset(amountPerAsset, quotes, masterAccount));
    }

    private RecommendedPortfolio createPortfolio() {
        List<PortfolioItem> items = List.of(
                PortfolioItem.create("PETR4", 30),
                PortfolioItem.create("VALE3", 25),
                PortfolioItem.create("ITUB4", 20),
                PortfolioItem.create("BBDC4", 15),
                PortfolioItem.create("WEGE3", 10));
        return RecommendedPortfolio.create("Test Portfolio", items, null);
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

    private BrokerageAccount createMasterAccountWithoutCustodies() {
        return BrokerageAccount.reconstruct(1L, null, "MASTER", null, null, new ArrayList<>());
    }

    private BrokerageAccount createMasterAccountWithCustodies() {
        BrokerageAccount account = BrokerageAccount.reconstruct(1L, null, "MASTER", null, null, new ArrayList<>());
        
        Custody petr4Custody = Custody.reconstruct(1L, account, "PETR4", 2, Money.create(new BigDecimal("35.00")), null);
        Custody vale3Custody = Custody.reconstruct(2L, account, "VALE3", 0, Money.create(new BigDecimal("62.00")), null);
        
        account.getCustodies().add(petr4Custody);
        account.getCustodies().add(vale3Custody);
        
        return account;
    }
}
