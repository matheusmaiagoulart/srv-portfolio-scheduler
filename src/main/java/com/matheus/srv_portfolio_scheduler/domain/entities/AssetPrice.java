package com.matheus.srv_portfolio_scheduler.domain.entities;

import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssetPrice {

    private long id;
    private LocalDate tradingDate;
    private String ticker;
    private Money openPrice;
    private Money closePrice;
    private Money maxPrice;
    private Money minPrice;

    public static AssetPrice reconstruct(long id, LocalDate tradingDate, String ticker, BigDecimal openPrice, BigDecimal closePrice, BigDecimal maxPrice, BigDecimal minPrice) {
        return AssetPrice.builder()
                .id(id)
                .tradingDate(tradingDate)
                .ticker(ticker)
                .openPrice(Money.create(openPrice))
                .closePrice(Money.create(closePrice))
                .maxPrice(Money.create(maxPrice))
                .minPrice(Money.create(minPrice))
                .build();
    }

    public static AssetPrice create(LocalDate tradingDate, String ticker, BigDecimal openPrice, BigDecimal closePrice, BigDecimal maxPrice, BigDecimal minPrice) {
        return AssetPrice.builder()
                .tradingDate(tradingDate)
                .ticker(ticker)
                .openPrice(Money.create(openPrice))
                .closePrice(Money.create(closePrice))
                .maxPrice(Money.create(maxPrice))
                .minPrice(Money.create(minPrice))
                .build();
    }
}
