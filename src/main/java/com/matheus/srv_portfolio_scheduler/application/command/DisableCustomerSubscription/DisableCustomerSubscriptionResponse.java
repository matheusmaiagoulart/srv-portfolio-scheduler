package com.matheus.srv_portfolio_scheduler.application.command.DisableCustomerSubscription;

public record DisableCustomerSubscriptionResponse(
        long customerId,
        String message
) {
    public static DisableCustomerSubscriptionResponse success(long customerId) {
        return new DisableCustomerSubscriptionResponse(
                customerId,
                "Customer subscription disabled successfully."
        );
    }
}

