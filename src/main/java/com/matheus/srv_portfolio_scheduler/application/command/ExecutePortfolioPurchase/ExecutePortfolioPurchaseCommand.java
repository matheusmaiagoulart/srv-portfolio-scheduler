package com.matheus.srv_portfolio_scheduler.application.command.ExecutePortfolioPurchase;

import com.matheus.srv_portfolio_scheduler.domain.services.dto.QuoteDTO;

import java.util.List;

public record ExecutePortfolioPurchaseCommand(
        List<QuoteDTO> lastCotahist) {
}
