package com.matheus.srv_portfolio_scheduler.domain.services.dto;

import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;

import java.util.Map;

public record PurchaseRoundDataDTO(
        Money totalPurchaseAmount,
        Map<Long, CustodyPurchaseDataDTO> custodiesPerCustomer) {
}
