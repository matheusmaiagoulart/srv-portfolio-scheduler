package com.matheus.srv_portfolio_scheduler.src.domain.services.rebalance;

import com.matheus.srv_portfolio_scheduler.domain.entities.Custody;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.CustomerBuyNeedDTO;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.PortfolioComparisonDTO;
import com.matheus.srv_portfolio_scheduler.domain.services.rebalance.CalculateBuyNeedOperation;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CalculateBuyNeedOperationTest {

    private final CalculateBuyNeedOperation operation = new CalculateBuyNeedOperation();

    @Test
    void deveCalcularNecessidadeDeCompraParaAtivoNovo() {
        Map<String, Custody> custodies = new HashMap<>();

        PortfolioComparisonDTO comparison = new PortfolioComparisonDTO(
                List.of(),
                List.of(),
                List.of(new PortfolioComparisonDTO.NewItem("RENT3", new BigDecimal("15"))));

        Map<String, Money> prices = Map.of("RENT3", Money.create(new BigDecimal("50.00")));

        Money originalValue = Money.create(new BigDecimal("1000.00"));
        Money availableAmount = Money.create(new BigDecimal("200.00"));

        CustomerBuyNeedDTO result = operation.execute(1L, custodies, comparison, prices, originalValue, availableAmount);

        assertTrue(result.neededAmountPerTicker().containsKey("RENT3"));
        assertEquals(new BigDecimal("150.00"), result.neededAmountPerTicker().get("RENT3").getAmount());
    }

    @Test
    void deveCalcularNecessidadeDeCompraParaAtivoQueAumentouPercentual() {
        Map<String, Custody> custodies = new HashMap<>();
        custodies.put("PETR4", createCustody("PETR4", 4, "45.00"));

        PortfolioComparisonDTO comparison = new PortfolioComparisonDTO(
                List.of(new PortfolioComparisonDTO.AlteredItem("PETR4", new BigDecimal("20"), new BigDecimal("25"))),
                List.of(),
                List.of());

        Map<String, Money> prices = Map.of("PETR4", Money.create(new BigDecimal("50.00")));

        Money originalValue = Money.create(new BigDecimal("1000.00"));
        Money availableAmount = Money.create(new BigDecimal("100.00"));

        CustomerBuyNeedDTO result = operation.execute(1L, custodies, comparison, prices, originalValue, availableAmount);

        assertTrue(result.neededAmountPerTicker().containsKey("PETR4"));
        assertEquals(new BigDecimal("50.00"), result.neededAmountPerTicker().get("PETR4").getAmount());
    }

    @Test
    void naoDeveGerarNecessidadeParaAtivosQueDiminuiramPercentual() {
        Map<String, Custody> custodies = new HashMap<>();
        custodies.put("PETR4", createCustody("PETR4", 10, "35.00"));

        PortfolioComparisonDTO comparison = new PortfolioComparisonDTO(
                List.of(new PortfolioComparisonDTO.AlteredItem("PETR4", new BigDecimal("30"), new BigDecimal("25"))),
                List.of(),
                List.of());

        Map<String, Money> prices = Map.of("PETR4", Money.create(new BigDecimal("40.00")));

        Money originalValue = Money.create(new BigDecimal("1000.00"));
        Money availableAmount = Money.create(new BigDecimal("100.00"));

        CustomerBuyNeedDTO result = operation.execute(1L, custodies, comparison, prices, originalValue, availableAmount);

        assertTrue(result.neededAmountPerTicker().isEmpty());
    }

    @Test
    void deveLimitarNecessidadeAoValorDisponivel() {
        Map<String, Custody> custodies = new HashMap<>();

        PortfolioComparisonDTO comparison = new PortfolioComparisonDTO(
                List.of(),
                List.of(),
                List.of(
                        new PortfolioComparisonDTO.NewItem("RENT3", new BigDecimal("15")),
                        new PortfolioComparisonDTO.NewItem("ABEV3", new BigDecimal("15"))));

        Map<String, Money> prices = Map.of(
                "RENT3", Money.create(new BigDecimal("50.00")),
                "ABEV3", Money.create(new BigDecimal("15.00")));

        Money originalValue = Money.create(new BigDecimal("1000.00"));
        Money availableAmount = Money.create(new BigDecimal("200.00"));

        CustomerBuyNeedDTO result = operation.execute(1L, custodies, comparison, prices, originalValue, availableAmount);

        BigDecimal totalNeed = result.neededAmountPerTicker().values().stream()
                .map(Money::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertTrue(totalNeed.compareTo(availableAmount.getAmount()) <= 0);
    }

    @Test
    void deveRetornarVazioQuandoNaoHouverValorDisponivel() {
        Map<String, Custody> custodies = new HashMap<>();

        PortfolioComparisonDTO comparison = new PortfolioComparisonDTO(
                List.of(),
                List.of(),
                List.of(new PortfolioComparisonDTO.NewItem("RENT3", new BigDecimal("15"))));

        Map<String, Money> prices = Map.of("RENT3", Money.create(new BigDecimal("50.00")));

        Money originalValue = Money.create(new BigDecimal("1000.00"));
        Money availableAmount = Money.create(BigDecimal.ZERO);

        CustomerBuyNeedDTO result = operation.execute(1L, custodies, comparison, prices, originalValue, availableAmount);

        assertTrue(result.neededAmountPerTicker().isEmpty());
    }

    @Test
    void deveIgnorarAtivoSemPreco() {
        Map<String, Custody> custodies = new HashMap<>();

        PortfolioComparisonDTO comparison = new PortfolioComparisonDTO(
                List.of(),
                List.of(),
                List.of(new PortfolioComparisonDTO.NewItem("RENT3", new BigDecimal("15"))));

        Map<String, Money> prices = Map.of();

        Money originalValue = Money.create(new BigDecimal("1000.00"));
        Money availableAmount = Money.create(new BigDecimal("200.00"));

        CustomerBuyNeedDTO result = operation.execute(1L, custodies, comparison, prices, originalValue, availableAmount);

        assertTrue(result.neededAmountPerTicker().isEmpty());
    }

    private Custody createCustody(String ticker, int quantity, String averagePrice) {
        return Custody.reconstruct(1L, null, ticker, quantity, Money.create(new BigDecimal(averagePrice)), null);
    }
}
