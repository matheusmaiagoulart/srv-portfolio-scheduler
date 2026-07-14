package com.matheus.srv_portfolio_scheduler.domain.services.dto;

import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;

public record TickerData(long purchaseId, int totalQuantity, Money assetPrice) {
}
