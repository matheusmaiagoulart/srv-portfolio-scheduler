package com.matheus.srv_portfolio_scheduler.application.ports.output.commands;

import com.matheus.srv_portfolio_scheduler.domain.entities.PortfolioRebalance;

import java.util.List;

public interface PortfolioRebalanceRepositoryPort {
    void saveAndFlushAll(List<PortfolioRebalance> portfolioRebalances);
}
