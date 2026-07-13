package com.matheus.srv_portfolio_scheduler.domain.events;

import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record IRDedoDuroEvent(
        String type,
        long customerId,
        String cpf,
        String ticker,
        String operationType,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal operationValue,
        BigDecimal aliquota,
        BigDecimal valueIR,
        LocalDateTime operationDate
) {
}
