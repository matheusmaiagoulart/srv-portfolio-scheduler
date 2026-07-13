package com.matheus.srv_portfolio_scheduler.application.command.ExecutePortfolioPurchase;

import com.matheus.srv_portfolio_scheduler.domain.services.dto.Distributions;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.DistributionsResultDTO;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.PurchaseOrdersPerAsset;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ExecutePortfolioPurchaseResponse(
        LocalDateTime executionDate,
        int totalCustomers,
        BigDecimal totalAmountExecuted,
        List<PurchaseOrdersPerAsset> purchaseOrders,
        List<Distributions> distributionsList,
        List<ResidualsFromMaster> residualsUsed,
        String message
) {
    public static ExecutePortfolioPurchaseResponse buildResponse(DistributionsResultDTO result) {
        return new ExecutePortfolioPurchaseResponse(
                LocalDateTime.now(),
                result.distributions().size(),
                result.purchaseOrdersPerAssets().stream().map(PurchaseOrdersPerAsset::totalPrice)
                        .reduce(Money.create(BigDecimal.ZERO).getAmount(), BigDecimal::add),
                result.purchaseOrdersPerAssets(),
                result.distributions(),
                result.residualsFromMaster(),
                "Compra programada executada com sucesso para " + result.distributions().size() + " clientes."
        );
    }
}
