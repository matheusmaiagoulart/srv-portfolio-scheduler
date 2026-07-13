package com.matheus.srv_portfolio_scheduler.domain.services.dto;

public record PurchaseOrdersPerAssetDetails(
        String type,
        String ticker,
        int quantity
) {
}
