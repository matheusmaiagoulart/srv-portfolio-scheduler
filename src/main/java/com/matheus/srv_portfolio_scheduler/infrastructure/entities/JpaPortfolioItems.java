package com.matheus.srv_portfolio_scheduler.infrastructure.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity(name = "portfolio_item")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JpaPortfolioItems {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommended_portfolio_id")
    private JpaRecommendedPortfolio recommendedPortfolio;

    private String ticker;
    private int percentage;

    public static JpaPortfolioItems create(String ticker, int percentage) {
        return JpaPortfolioItems.builder()
                .ticker(ticker)
                .percentage(percentage)
                .build();
    }
}
