package com.matheus.srv_portfolio_scheduler.adapters.output.commands;

import com.matheus.srv_portfolio_scheduler.adapters.mapper.AssetPriceMapper;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.AssetPriceRepositoryPort;
import com.matheus.srv_portfolio_scheduler.domain.entities.AssetPrice;
import com.matheus.srv_portfolio_scheduler.infrastructure.entities.JpaAssetPrice;
import com.matheus.srv_portfolio_scheduler.infrastructure.persistence.JpaAssetPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AssetPriceRepositoryAdapter implements AssetPriceRepositoryPort {

    private final JpaAssetPriceRepository jpaAssetPriceRepository;

    @Override
    @Transactional
    public void saveAll(List<AssetPrice> assetsPricesList) {
        List<JpaAssetPrice> toSave = new ArrayList<>();

        for (AssetPrice asset : assetsPricesList) {
            Optional<JpaAssetPrice> existing = jpaAssetPriceRepository
                    .findByTradingDateAndTicker(asset.getTradingDate(), asset.getTicker());

            if (existing.isPresent()) {
                JpaAssetPrice jpa = existing.get();
                jpa.setOpenPrice(asset.getOpenPrice());
                jpa.setClosePrice(asset.getClosePrice());
                jpa.setMaxPrice(asset.getMaxPrice());
                jpa.setMinPrice(asset.getMinPrice());
                toSave.add(jpa);
            } else {
                toSave.add(AssetPriceMapper.toJpaEntity(asset));
            }
        }

        jpaAssetPriceRepository.saveAllAndFlush(toSave);
    }
}
