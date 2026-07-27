package com.matheus.srv_portfolio_scheduler.domain.services.dto;

import com.matheus.srv_portfolio_scheduler.application.command.ExecutePortfolioPurchase.ResidualsFromMaster;

import java.util.List;

public record PurchaseSummaryDTO(
        List<PurchaseOrdersPerAsset> purchaseOrdersPerAssets,
        List<ResidualsFromMaster> residualsFromMaster,
        int totalCustomersProcessed,
        int totalDeliveries,
        int totalOutboxEntries
) {
}

