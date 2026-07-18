package com.matheus.srv_portfolio_scheduler.infrastructure.queries;

import com.matheus.srv_portfolio_scheduler.infrastructure.entities.JpaRecommendedPortfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaRecommendedPortfolioQueryRepository extends JpaRepository<JpaRecommendedPortfolio, Long> {
}
