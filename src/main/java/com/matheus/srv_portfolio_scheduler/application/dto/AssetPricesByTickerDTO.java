package com.matheus.srv_portfolio_scheduler.application.dto;

import java.math.BigDecimal;

public record AssetPricesByTickerDTO(
        String ticker,
        BigDecimal closePrice
) {
}
