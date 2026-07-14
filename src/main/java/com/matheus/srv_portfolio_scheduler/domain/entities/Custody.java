package com.matheus.srv_portfolio_scheduler.domain.entities;

import com.matheus.srv_portfolio_scheduler.domain.enums.BrokerageAccountType;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Custody {

    private Long id;

    private BrokerageAccount brokerageAccount;

    private String ticker;
    private int quantity;
    private Money averagePrice;
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

    public void addPurchaseQuantity(int quantity, Money price) {
        updateAveragePrice(quantity, price);
        this.quantity += quantity;
        this.lastUpdate = OffsetDateTime.now();
    }

    public void updateAveragePrice(int newQuantity, Money newPrice) {
        int totalQuantity = this.quantity + newQuantity;
        if (totalQuantity == 0) this.averagePrice = Money.create(BigDecimal.ZERO);

        // (averagePrice * quantity) + (newPrice * newQuantity) / totalQuantity
        BigDecimal newAveragePrice = (averagePrice.getAmount().multiply(BigDecimal.valueOf(quantity)))
                .add(newPrice.getAmount().multiply(BigDecimal.valueOf(newQuantity)))
                .divide(BigDecimal.valueOf(totalQuantity), 2, RoundingMode.HALF_UP);

        this.averagePrice = Money.create(newAveragePrice);
    }

    public void updateResidualQuantity(int residualQuantity, Money price, BrokerageAccountType accountType) {
        if (accountType == BrokerageAccountType.MASTER) {
            this.quantity = residualQuantity;
            updateAveragePrice(residualQuantity, price);
            this.lastUpdate = OffsetDateTime.now();
        }
    }
}
