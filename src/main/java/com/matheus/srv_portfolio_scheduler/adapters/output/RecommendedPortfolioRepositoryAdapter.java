package com.matheus.srv_portfolio_scheduler.adapters.output;

import com.matheus.srv_portfolio_scheduler.adapters.mapper.RecommendedPortfolioMapper;
import com.matheus.srv_portfolio_scheduler.application.ports.output.RecommendedPortfolioRepositoryPort;
import com.matheus.srv_portfolio_scheduler.domain.entities.RecommendedPortfolio;
import com.matheus.srv_portfolio_scheduler.infrastructure.entities.JpaRecommendedPortfolio;
import com.matheus.srv_portfolio_scheduler.infrastructure.persistence.JpaRecommendedPortfolioRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@AllArgsConstructor
public class RecommendedPortfolioRepositoryAdapter implements RecommendedPortfolioRepositoryPort {

    private final JpaRecommendedPortfolioRepository repository;

    @Override
    public void save(RecommendedPortfolio recommendedPortfolio) {
        try {
            JpaRecommendedPortfolio jpaEntity = RecommendedPortfolioMapper.toJpaEntity(recommendedPortfolio);
            repository.save(jpaEntity);
        } catch (Exception e) {
            log.error("Error saving recommended portfolio: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save recommended portfolio", e);
        }
    }

    @Override
    public Optional<RecommendedPortfolio> getActiveRecommendedPortfolio() {
        return repository.getActiveRecommendedPortfolio()
                .map(RecommendedPortfolioMapper::toDomain);
    }
}
