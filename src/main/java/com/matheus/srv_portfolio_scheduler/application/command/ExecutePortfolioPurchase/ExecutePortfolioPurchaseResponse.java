package com.matheus.srv_portfolio_scheduler.application.command.ExecutePortfolioPurchase;

import com.matheus.srv_portfolio_scheduler.domain.services.dto.PurchaseOrdersPerAsset;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.PurchaseSummaryDTO;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

public record ExecutePortfolioPurchaseResponse(
        LocalDateTime executionDate,
        int totalCustomersProcessed,
        int totalDeliveries,
        int totalOutboxEntries,
        String totalAmountExecuted,
        List<PurchaseOrdersPerAsset> purchaseOrdersPerAssets,
        List<ResidualsFromMaster> residualsFromMaster,
        String message
) {
    public static ExecutePortfolioPurchaseResponse buildResponse(PurchaseSummaryDTO result) {
        BigDecimal totalAmount = result.purchaseOrdersPerAssets().stream()
                .map(PurchaseOrdersPerAsset::totalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String formattedAmount = NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(totalAmount);

        return new ExecutePortfolioPurchaseResponse(
                LocalDateTime.now(),
                result.totalCustomersProcessed(),
                result.totalDeliveries(),
                result.totalOutboxEntries(),
                formattedAmount,
                result.purchaseOrdersPerAssets(),
                result.residualsFromMaster(),
                "Compra programada executada com sucesso para " + result.totalCustomersProcessed() + " clientes."
        );
    }
}
