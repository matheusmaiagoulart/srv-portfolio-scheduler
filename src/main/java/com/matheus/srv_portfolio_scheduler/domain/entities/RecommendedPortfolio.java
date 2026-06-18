package com.matheus.srv_portfolio_scheduler.domain.entities;

import com.matheus.srv_portfolio_scheduler.domain.exceptions.BusinessException;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecommendedPortfolio {

    private long id;
    private String name;
    private boolean active;
    private OffsetDateTime createdAt;
    private OffsetDateTime terminationDate;
    private List<PortfolioItem> portfolioItems;

    public static RecommendedPortfolio create(String name, List<PortfolioItem> portfolioItems) {

        if (name == null || name.isBlank())
            throw new BusinessException("INVALID_PORTFOLIO_NAME", "A recommended portfolio must have a name.");

        if (portfolioItems == null || portfolioItems.isEmpty() || portfolioItems.stream().count() != 5)
            throw new BusinessException("INVALID_PORTFOLIO_ITEMS", "A recommended portfolio must have exactly 5 items.");

        if (portfolioItems.stream().map(PortfolioItem::getTicker).distinct().count() != 5) {
            throw new BusinessException("DUPLICATED_PORTFOLIO_ITEMS", "The list items had duplicated tickers.");
        }

        if (portfolioItems.stream().mapToInt(PortfolioItem::getPercentage).sum() != 100) {
            throw new BusinessException("INVALID_TOTAL_PERCENTAGE","The percentage of each item must be greater than 0 and the total " +
                    "percentage must be equal 100.");
        }

        RecommendedPortfolio portfolio = RecommendedPortfolio.builder()
                .name(name)
                .active(true)
                .createdAt(OffsetDateTime.now())
                .portfolioItems(portfolioItems)
                .build();

        portfolioItems.forEach(item -> item.setRecommendedPortfolio(portfolio));

        return portfolio;
    }

    public void deactivate() {
        this.active = false;
        this.terminationDate = OffsetDateTime.now();
    }
}
