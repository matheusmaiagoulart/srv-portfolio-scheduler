package com.matheus.srv_portfolio_scheduler.application.ports.output;

import com.matheus.srv_portfolio_scheduler.domain.entities.AssetPrice;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;

import java.util.List;
import java.util.Map;

public interface AssetPriceRepositoryPort {
    void saveAll(List<AssetPrice> assetsPricesList);
    Map<String, Money> getAssetsPrices(List<String> assets);
}
