package com.matheus.srv_portfolio_scheduler.application.command.UpdateMonthlyAmount;

import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;

public record UpdateMonthlyAmountResponse(
        long customerId,
        Money lastMonthlyAmount,
        Money newMonthlyAmount,
        String message
) {
    public static UpdateMonthlyAmountResponse success(long customerId, Money lastMonthlyAmount, Money newMonthlyAmount) {
        return new UpdateMonthlyAmountResponse(
                customerId,
                lastMonthlyAmount,
                newMonthlyAmount,
                "Monthly amount updated successfully. The new value will be considered at next billing cycle."
        );
    }
}


