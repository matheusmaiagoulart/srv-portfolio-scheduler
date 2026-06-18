package com.matheus.srv_portfolio_scheduler.adapters.output.mapper;

import com.matheus.srv_portfolio_scheduler.domain.entities.PortfolioItem;
import com.matheus.srv_portfolio_scheduler.infrastructure.entities.JpaPortfolioItems;

public class PortfolioItemMapper {

    public static JpaPortfolioItems toJpaEntity(PortfolioItem portfolioItem) {
        return JpaPortfolioItems.create(
                portfolioItem.getTicker(),
                portfolioItem.getPercentage()
        );
    }

    public static PortfolioItem toDomain(JpaPortfolioItems portfolioItem) {
        return PortfolioItem.create(
                portfolioItem.getTicker(),
                portfolioItem.getPercentage()
        );
    }
}
