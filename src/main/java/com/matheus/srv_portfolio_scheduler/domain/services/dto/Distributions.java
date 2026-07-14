package com.matheus.srv_portfolio_scheduler.domain.services.dto;

import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;

import java.util.List;

public record Distributions(
        long customerId,
        String fullName,
        Money thirdPartyBalance,
        List<DistributionsPerAsset> distributionsPerAssets
) {
}
