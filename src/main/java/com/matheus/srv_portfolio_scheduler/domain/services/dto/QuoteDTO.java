package com.matheus.srv_portfolio_scheduler.domain.services.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record QuoteDTO(
        LocalDate dataPregao,
        String ticker,
        String bdiCode,
        int marketType,
        String companyName,
        BigDecimal openPrice,
        BigDecimal maxPrice,
        BigDecimal minPrice,
        BigDecimal closePrice,
        BigDecimal averagePrice,
        long tradeQuantity,
        BigDecimal tradedVolume
) {}
