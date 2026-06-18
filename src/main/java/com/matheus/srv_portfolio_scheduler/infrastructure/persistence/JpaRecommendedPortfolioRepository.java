package com.matheus.srv_portfolio_scheduler.infrastructure.persistence;

import com.matheus.srv_portfolio_scheduler.infrastructure.entities.JpaRecommendedPortfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaRecommendedPortfolioRepository extends JpaRepository<JpaRecommendedPortfolio, Long> {

    @Query("SELECT rp FROM recommended_portfolios rp WHERE rp.active = true")
    Optional<JpaRecommendedPortfolio> getActiveRecommendedPortfolio();
}
