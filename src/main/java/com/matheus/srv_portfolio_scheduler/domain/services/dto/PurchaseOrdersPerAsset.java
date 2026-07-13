package com.matheus.srv_portfolio_scheduler.domain.services.dto;

import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;

import java.math.BigDecimal;

public record PurchaseOrdersPerAsset(
        String ticker,
        int totalQuantity,
        PurchaseOrdersPerAssetDetails details,
        Money unitPrice
) {
    public BigDecimal totalPrice() {
        return unitPrice.getAmount().multiply(BigDecimal.valueOf(totalQuantity));
    }
}

