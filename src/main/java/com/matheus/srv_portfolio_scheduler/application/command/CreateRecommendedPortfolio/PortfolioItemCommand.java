package com.matheus.srv_portfolio_scheduler.application.command.CreateRecommendedPortfolio;

import com.matheus.srv_portfolio_scheduler.domain.entities.PortfolioItem;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record PortfolioItemCommand(
        @NotBlank(message = "Ticker is required.")
        String ticker,

        @Min(value = 1, message = "Percentage must be greater than 0.")
        @Max(value = 100, message = "Percentage must be less than or equal to 100.")
        int percentage
) {
        public PortfolioItem toDomain() {
                return PortfolioItem.create(this.ticker, this.percentage);
        }
}
