package com.matheus.srv_portfolio_scheduler.application.command.PortfolioRebalance;

import com.matheus.srv_portfolio_scheduler.domain.services.dto.PortfolioComparisonDTO;

public record PortfolioRebalanceCommand(PortfolioComparisonDTO portfolioComparisonDTO) {
}
