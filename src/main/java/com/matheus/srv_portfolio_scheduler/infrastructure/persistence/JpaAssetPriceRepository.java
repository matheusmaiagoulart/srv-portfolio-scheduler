package com.matheus.srv_portfolio_scheduler.infrastructure.persistence;

import com.matheus.srv_portfolio_scheduler.infrastructure.entities.JpaAssetPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface JpaAssetPriceRepository extends JpaRepository<JpaAssetPrice, Long> {
    boolean existsByTradingDate(LocalDate tradingDate);
    Optional<JpaAssetPrice> findByTradingDateAndTicker(LocalDate tradingDate, String ticker);
}
