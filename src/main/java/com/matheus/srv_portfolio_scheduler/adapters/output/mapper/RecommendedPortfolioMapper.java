package com.matheus.srv_portfolio_scheduler.adapters.output.mapper;

import com.matheus.srv_portfolio_scheduler.domain.entities.RecommendedPortfolio;
import com.matheus.srv_portfolio_scheduler.infrastructure.entities.JpaPortfolioItems;
import com.matheus.srv_portfolio_scheduler.infrastructure.entities.JpaRecommendedPortfolio;

import java.util.List;

public class RecommendedPortfolioMapper {

    public static JpaRecommendedPortfolio toJpaEntity(RecommendedPortfolio portfolio) {
        List<JpaPortfolioItems> items = portfolio.getPortfolioItems().stream()
                .map(PortfolioItemMapper::toJpaEntity)
                .toList();

        JpaRecommendedPortfolio portfolioEntity = JpaRecommendedPortfolio.builder()
                .id(portfolio.getId())
                .name(portfolio.getName())
                .active(portfolio.isActive())
                .createdAt(portfolio.getCreatedAt())
                .terminationDate(portfolio.getTerminationDate())
                .portfolioItems(items)
                .build();

        items.forEach(item -> item.setRecommendedPortfolio(portfolioEntity));

        return portfolioEntity;
    }

    public static RecommendedPortfolio toDomain(JpaRecommendedPortfolio portfolioEntity) {
        return RecommendedPortfolio.builder()
                .id(portfolioEntity.getId())
                .name(portfolioEntity.getName())
                .active(portfolioEntity.isActive())
                .createdAt(portfolioEntity.getCreatedAt())
                .terminationDate(portfolioEntity.getTerminationDate())
                .portfolioItems(portfolioEntity.getPortfolioItems().stream().map(PortfolioItemMapper::toDomain).toList())
                .build();
    }
}
