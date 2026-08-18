package com.matheus.srv_portfolio_scheduler.application.event;

import com.matheus.srv_portfolio_scheduler.domain.services.dto.PortfolioComparisonDTO;

public record PortfolioRebalanceRequestedEvent(
        PortfolioComparisonDTO portfolioComparisonDTO
) {
}

