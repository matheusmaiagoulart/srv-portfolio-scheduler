package com.matheus.srv_portfolio_scheduler.domain.services.dto;

import com.matheus.srv_portfolio_scheduler.domain.entities.Custody;
import com.matheus.srv_portfolio_scheduler.domain.entities.Delivery;

import java.util.List;
import java.util.Map;

public record DistributionOutput(
        List<Delivery> deliveries,
        List<Distributions> distributions,
        Map<String, Integer> distributedPerTicker,
        List<Custody> modifiedCustodies
) {
}
