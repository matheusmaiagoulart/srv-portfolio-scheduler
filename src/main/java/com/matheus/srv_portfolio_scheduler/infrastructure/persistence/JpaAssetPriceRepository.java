package com.matheus.srv_portfolio_scheduler.infrastructure.persistence;

import com.matheus.srv_portfolio_scheduler.application.dto.AssetPricesByTickerDTO;
import com.matheus.srv_portfolio_scheduler.infrastructure.entities.JpaAssetPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface JpaAssetPriceRepository extends JpaRepository<JpaAssetPrice, Long> {

    @Query("""
    SELECT a.ticker, a.closePrice.amount
            FROM asset_prices a
            WHERE a.ticker IN :assets
    """)
    List<AssetPricesByTickerDTO> getAssetsPrices(List<String> assets);
}
