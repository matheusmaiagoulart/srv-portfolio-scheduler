package com.matheus.srv_portfolio_scheduler.domain.services.dto;

import com.matheus.srv_portfolio_scheduler.domain.entities.Custody;
import com.matheus.srv_portfolio_scheduler.domain.entities.Delivery;

import java.util.List;

public record DistributionsResultDTO(
        List<PurchaseOrdersPerAsset> purchaseOrdersPerAssets,
        List<Distributions> distributions,
        List<Delivery> deliveries,
        List<Custody> modifiedCustodies
) {
}
