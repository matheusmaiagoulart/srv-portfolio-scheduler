package com.matheus.srv_portfolio_scheduler.application.command.CreateRecommendedPortfolio;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.List;

public record CreateRecommendedPortfolioCommand(
        @NotBlank(message = "Name is required.")
        @Size(min = 1, max = 100, message = "Name cannot exceed 100 characters.")
        String name,

        @NotNull(message = "Portfolio items are required.")
        @Size(min = 5, max = 5, message = "Portfolio must have exactly 5 items.")
        @Valid
        List<PortfolioItemCommand> portfolioItems,
        @Future(message = "The Date must be in the future.")
        OffsetDateTime terminationDate)
{
}