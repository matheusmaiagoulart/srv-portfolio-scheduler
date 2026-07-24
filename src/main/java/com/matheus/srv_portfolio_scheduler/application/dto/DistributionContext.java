package com.matheus.srv_portfolio_scheduler.application.dto;

import com.matheus.srv_portfolio_scheduler.domain.entities.Custody;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.TickerData;

import java.util.Map;

public record DistributionContext(
        Map<String, Custody> masterCustodies,
        Map<String, Integer> distributedPerTicker,
        Map<String, TickerData> purchasedPerTicker,
        Map<String, TickerData> totalPerTicker
) {
}