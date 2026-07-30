package com.matheus.srv_portfolio_scheduler.application.command.UpdateMonthlyAmount;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record UpdateMonthlyAmountCommand(
        @NotNull(message = "Monthly amount cannot be null.")
        @DecimalMin(value = "100", message = "Monthly amount must be at least 100.")
        double newMonthlyAmount
) {
}


