package com.matheus.srv_portfolio_scheduler.adapters.mapper;

import com.matheus.srv_portfolio_scheduler.domain.entities.PortfolioItem;
import com.matheus.srv_portfolio_scheduler.infrastructure.entities.JpaPortfolioItem;

public class PortfolioItemMapper {

    public static JpaPortfolioItem toJpaEntity(PortfolioItem portfolioItem) {
        return JpaPortfolioItem.create(
                portfolioItem.getTicker(),
                portfolioItem.getPercentage()
        );
    }

    public static PortfolioItem toDomain(JpaPortfolioItem portfolioItem) {
        return PortfolioItem.create(
                portfolioItem.getTicker(),
                portfolioItem.getPercentage()
        );
    }
}
