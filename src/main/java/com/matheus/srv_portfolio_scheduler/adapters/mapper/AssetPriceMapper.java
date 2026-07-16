package com.matheus.srv_portfolio_scheduler.adapters.mapper;

import com.matheus.srv_portfolio_scheduler.domain.entities.AssetPrice;
import com.matheus.srv_portfolio_scheduler.infrastructure.entities.JpaAssetPrice;

public class AssetPriceMapper {

    public static JpaAssetPrice toJpaEntity(AssetPrice assetPrice) {
        return JpaAssetPrice.builder()
                .TradingDate(assetPrice.getTradingDate())
                .ticker(assetPrice.getTicker())
                .openPrice(assetPrice.getOpenPrice())
                .closePrice(assetPrice.getClosePrice())
                .maxPrice(assetPrice.getMaxPrice())
                .minPrice(assetPrice.getMinPrice())
                .build();
    }

    public static AssetPrice toDomain(JpaAssetPrice jpaAssetPrice) {
        return AssetPrice.reconstruct(
                jpaAssetPrice.getId(),
                jpaAssetPrice.getTradingDate(),
                jpaAssetPrice.getTicker(),
                jpaAssetPrice.getOpenPrice().getAmount(),
                jpaAssetPrice.getClosePrice().getAmount(),
                jpaAssetPrice.getMaxPrice().getAmount(),
                jpaAssetPrice.getMinPrice().getAmount()
        );
    }
}


