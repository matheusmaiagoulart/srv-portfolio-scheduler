package com.matheus.srv_portfolio_scheduler.infrastructure.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Entity(name = "recommended_portfolios")
@Table(name = "recommended_portfolios")
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JpaRecommendedPortfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;
    private boolean active;
    private OffsetDateTime createdAt;
    private OffsetDateTime terminationDate;

    @OneToMany(
            mappedBy = "recommendedPortfolio",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<JpaPortfolioItem> portfolioItems;

    public static JpaRecommendedPortfolio create(String name, List<JpaPortfolioItem> portfolioItems) {

        JpaRecommendedPortfolio portfolio = JpaRecommendedPortfolio.builder()
                .name(name)
                .active(true)
                .createdAt(OffsetDateTime.now())
                .portfolioItems(portfolioItems)
                .build();

        portfolioItems.forEach(item -> item.setRecommendedPortfolio(portfolio));

        return portfolio;
    }
}
