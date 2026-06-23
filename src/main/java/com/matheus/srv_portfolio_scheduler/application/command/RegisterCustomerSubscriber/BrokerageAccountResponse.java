package com.matheus.srv_portfolio_scheduler.application.command.RegisterCustomerSubscriber;

import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record BrokerageAccountResponse(
        long id,
        String accountNumber,
        String accountType,
        OffsetDateTime createdAt
) {
}
