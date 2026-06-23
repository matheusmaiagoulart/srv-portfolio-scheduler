package com.matheus.srv_portfolio_scheduler.application.command.RegisterCustomerSubscriber;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record RegisterCustomerSubscriberCommand(

        @NotBlank(message = "Name cannot be null or empty.")
        @Size(min = 1, max = 100, message = "Name length must be between 1 and 100.")
        String name,

        @NotBlank(message = "Cpf cannot be null or empty.")
        @Size(min = 11, max = 11, message = "Cpf length must be 11 characters.")
        String cpf,

        @Email
        @NotBlank(message = "Name cannot be null or empty.")
        String email,

        @Min(value = 100, message = "Monthly Amount must be at least 100.")
        BigDecimal monthlyAmount
        ) {
}
