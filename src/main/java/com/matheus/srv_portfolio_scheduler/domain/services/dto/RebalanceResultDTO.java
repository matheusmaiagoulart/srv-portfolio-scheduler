package com.matheus.srv_portfolio_scheduler.domain.services.dto;

import com.matheus.srv_portfolio_scheduler.domain.entities.PortfolioRebalance;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;

import java.util.List;

public record RebalanceResultDTO(Money totalReleased, List<PortfolioRebalance> auditRecords) {
}
