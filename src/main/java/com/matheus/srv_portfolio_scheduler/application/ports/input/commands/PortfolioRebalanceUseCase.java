package com.matheus.srv_portfolio_scheduler.application.ports.input.commands;

import com.matheus.srv_portfolio_scheduler.application.command.PortfolioRebalance.PortfolioRebalanceCommand;
import com.matheus.srv_portfolio_scheduler.application.command.PortfolioRebalance.PortfolioRebalanceResponse;

public interface PortfolioRebalanceUseCase {
    PortfolioRebalanceResponse handler(PortfolioRebalanceCommand command);
}
