package com.matheus.srv_portfolio_scheduler.domain.entities;

import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Custody {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brokerage_account_id")
    private BrokerageAccount brokerageAccount;

    private String ticker;
    private int quantity;
    @Embedded
    public Money averagePrice;
    private OffsetDateTime lastUpdate;

    public static Custody create(BrokerageAccount brokerageAccount, String ticker) {
        return Custody.builder()
                .brokerageAccount(brokerageAccount)
                .ticker(ticker)
                .quantity(0)
                .averagePrice(Money.create(BigDecimal.ZERO))
                .lastUpdate(OffsetDateTime.now())
                .build();
    }

    public static Custody reconstruct(Long id, BrokerageAccount brokerageAccount, String ticker, int quantity, Money averagePrice, OffsetDateTime lastUpdate) {
        return Custody.builder()
                .id(id)
                .brokerageAccount(brokerageAccount)
                .ticker(ticker)
                .quantity(quantity)
                .averagePrice(averagePrice)
                .lastUpdate(lastUpdate)
                .build();
    }
}
