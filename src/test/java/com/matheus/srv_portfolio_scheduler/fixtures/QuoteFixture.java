package com.matheus.srv_portfolio_scheduler.fixtures;

import com.matheus.srv_portfolio_scheduler.domain.services.dto.QuoteDTO;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class QuoteFixture {

    private LocalDate tradingDate = LocalDate.of(2026, 3, 27);
    private String ticker = TestDefaults.TICKER;
    private BigDecimal closePrice = TestDefaults.UNIT_PRICE;

    private QuoteFixture() {
    }

    public static QuoteFixture aQuote() {
        return new QuoteFixture();
    }

    public QuoteFixture withTradingDate(LocalDate tradingDate) {
        this.tradingDate = tradingDate;
        return this;
    }

    public QuoteFixture withTicker(String ticker) {
        this.ticker = ticker;
        return this;
    }

    public QuoteFixture withClosePrice(String closePrice) {
        this.closePrice = new BigDecimal(closePrice);
        return this;
    }

    public QuoteDTO build() {
        return new QuoteDTO(
                tradingDate,
                ticker,
                "02",
                10,
                "Test Company",
                closePrice.subtract(BigDecimal.ONE),
                closePrice.add(BigDecimal.ONE),
                closePrice.subtract(new BigDecimal("2.00")),
                closePrice,
                closePrice,
                1000L,
                new BigDecimal("100000.00"));
    }
}