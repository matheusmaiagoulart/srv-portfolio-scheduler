package com.matheus.srv_portfolio_scheduler.infrastructure.queries;

import com.matheus.srv_portfolio_scheduler.application.dto.AssetPricesByTickerDTO;
import com.matheus.srv_portfolio_scheduler.infrastructure.entities.JpaAssetPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JpaAssetPriceQueryRepository extends JpaRepository<JpaAssetPrice, Long> {

    @Query("""
    SELECT a.ticker, a.closePrice.amount
            FROM asset_prices a
            WHERE a.ticker IN :assets
    """)
    List<AssetPricesByTickerDTO> getAssetsPrices(List<String> assets);

    @Query("""
    SELECT MAX(a.tradingDate)
            FROM asset_prices a
    """)
    Optional<LocalDate> getLatestTradingDate();

    @Query("""
    SELECT a
            FROM asset_prices a
            WHERE a.tradingDate = :tradingDate
    """)
    List<JpaAssetPrice> getJpaAssetPriceByTradingDate(@Param("tradingDate") LocalDate tradingDate);
}
