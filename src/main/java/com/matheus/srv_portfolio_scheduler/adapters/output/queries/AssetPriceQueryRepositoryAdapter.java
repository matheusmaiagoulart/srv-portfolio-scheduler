package com.matheus.srv_portfolio_scheduler.adapters.output.queries;

import com.matheus.srv_portfolio_scheduler.application.dto.AssetPricesByTickerDTO;
import com.matheus.srv_portfolio_scheduler.application.ports.output.queries.AssetPriceQueryRepositoryPort;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import com.matheus.srv_portfolio_scheduler.infrastructure.queries.JpaAssetPriceQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AssetPriceQueryRepositoryAdapter implements AssetPriceQueryRepositoryPort {

    private final JpaAssetPriceQueryRepository jpaAssetPriceRepository;

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
