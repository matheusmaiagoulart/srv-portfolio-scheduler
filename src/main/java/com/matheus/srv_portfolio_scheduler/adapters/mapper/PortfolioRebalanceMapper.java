package com.matheus.srv_portfolio_scheduler.adapters.mapper;

import com.matheus.srv_portfolio_scheduler.domain.entities.PortfolioRebalance;
import com.matheus.srv_portfolio_scheduler.infrastructure.entities.JpaPortfolioRebalance;

public class PortfolioRebalanceMapper {

    public static JpaPortfolioRebalance toJpaEntity(PortfolioRebalance rebalance) {
        return JpaPortfolioRebalance.builder()
                .id(rebalance.getId())
                .customerId(rebalance.getCustomerId())
                .rebalanceType(rebalance.getRebalanceType())
                .soldTicker(rebalance.getSoldTicker())
                .boughtTicker(rebalance.getBoughtTicker())
                .soldAmount(rebalance.getSoldAmount())
                .rebalanceDate(rebalance.getRebalanceDate())
                .build();
    }

    public static PortfolioRebalance toDomain(JpaPortfolioRebalance jpaRebalance) {
        return PortfolioRebalance.reconstruct(
                jpaRebalance.getId(),
                jpaRebalance.getCustomerId(),
                jpaRebalance.getRebalanceType(),
                jpaRebalance.getSoldTicker(),
                jpaRebalance.getBoughtTicker(),
                jpaRebalance.getSoldAmount(),
                jpaRebalance.getRebalanceDate()
        );
    }
}
