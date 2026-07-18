package com.matheus.srv_portfolio_scheduler.application.ports.output.queries;

import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;

import java.util.List;
import java.util.Map;

public interface AssetPriceQueryRepositoryPort {
    Map<String, Money> getAssetsPrices(List<String> assets);
}
