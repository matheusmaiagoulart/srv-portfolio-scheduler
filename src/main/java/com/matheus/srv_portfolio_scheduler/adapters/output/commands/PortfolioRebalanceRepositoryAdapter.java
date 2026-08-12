package com.matheus.srv_portfolio_scheduler.adapters.output.commands;

import com.matheus.srv_portfolio_scheduler.adapters.mapper.PortfolioRebalanceMapper;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.PortfolioRebalanceRepositoryPort;
import com.matheus.srv_portfolio_scheduler.domain.entities.PortfolioRebalance;
import com.matheus.srv_portfolio_scheduler.infrastructure.persistence.JpaPortfolioRebalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PortfolioRebalanceRepositoryAdapter implements PortfolioRebalanceRepositoryPort {

    private final JpaPortfolioRebalanceRepository jpaPortfolioRebalanceRepository;

    @Override
    public void saveAndFlushAll(List<PortfolioRebalance> portfolioRebalances) {
        jpaPortfolioRebalanceRepository.saveAllAndFlush(portfolioRebalances.stream()
                .map(PortfolioRebalanceMapper::toJpaEntity)
                .toList());

    }
}
