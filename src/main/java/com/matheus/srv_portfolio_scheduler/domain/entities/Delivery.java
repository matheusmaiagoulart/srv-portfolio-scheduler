package com.matheus.srv_portfolio_scheduler.domain.entities;

import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Delivery {

    private long id;
    private long purchaseOrderId;
    private long custodyCustomerId;
    private String ticker;
    private int quantity;
    private Money unitPrice;
    private LocalDateTime deliveryDate;

    public static Delivery createDelivery(long purchaseOrderId, long custodyCustomerId, String ticker, int quantity, Money unitPrice) {
        return Delivery.builder()
                .purchaseOrderId(purchaseOrderId)
                .custodyCustomerId(custodyCustomerId)
                .ticker(ticker)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .deliveryDate(LocalDateTime.now())
                .build();
    }

    public static Delivery reconstruct(long id, long purchaseOrderId, long custodyCustomerId, String ticker, int quantity, Money unitPrice, LocalDateTime deliveryDate) {
        return Delivery.builder()
                .id(id)
                .purchaseOrderId(purchaseOrderId)
                .custodyCustomerId(custodyCustomerId)
                .ticker(ticker)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .deliveryDate(deliveryDate)
                .build();
    }
}
