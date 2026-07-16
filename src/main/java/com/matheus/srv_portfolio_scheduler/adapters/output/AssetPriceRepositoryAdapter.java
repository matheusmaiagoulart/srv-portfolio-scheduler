package com.matheus.srv_portfolio_scheduler.adapters.output;

import com.matheus.srv_portfolio_scheduler.adapters.mapper.AssetPriceMapper;
import com.matheus.srv_portfolio_scheduler.application.dto.AssetPricesByTickerDTO;
import com.matheus.srv_portfolio_scheduler.application.ports.output.AssetPriceRepositoryPort;
import com.matheus.srv_portfolio_scheduler.domain.entities.AssetPrice;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import com.matheus.srv_portfolio_scheduler.infrastructure.entities.JpaAssetPrice;
import com.matheus.srv_portfolio_scheduler.infrastructure.persistence.JpaAssetPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AssetPriceRepositoryAdapter implements AssetPriceRepositoryPort {

    private final JpaAssetPriceRepository jpaAssetPriceRepository;

    @Override
    public void saveAll(List<AssetPrice> assetsPricesList) {
        List<JpaAssetPrice> jpaAssetPrices = assetsPricesList.stream()
                .map(AssetPriceMapper::toJpaEntity).toList();

        jpaAssetPriceRepository.saveAll(jpaAssetPrices);
    }

    @Override
    public Map<String, Money> getAssetsPrices(List<String> assets) {
        return jpaAssetPriceRepository.getAssetsPrices(assets)
                .stream()
                .collect(Collectors.toMap(
                        AssetPricesByTickerDTO::ticker,
                        dto -> Money.create(dto.closePrice())
                ));
    }
}
