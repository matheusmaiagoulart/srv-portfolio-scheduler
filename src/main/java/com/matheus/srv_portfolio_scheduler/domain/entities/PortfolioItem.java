package com.matheus.srv_portfolio_scheduler.domain.entities;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioItem {

    private long id;

    @Setter
    private RecommendedPortfolio recommendedPortfolio;

    private String ticker;
    private int percentage;

    public static PortfolioItem create(String ticker, int percentage) {
        return PortfolioItem.builder()
                .ticker(ticker)
                .percentage(percentage)
                .build();
    }
}
