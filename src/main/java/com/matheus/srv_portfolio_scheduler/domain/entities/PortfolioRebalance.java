package com.matheus.srv_portfolio_scheduler.domain.entities;

import com.matheus.srv_portfolio_scheduler.domain.enums.RebalanceType;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PortfolioRebalance {

    private Long id;
    private Long customerId;
    private RebalanceType rebalanceType;
    private String soldTicker;
    private String boughtTicker;
    private Money soldAmount;
    private LocalDateTime rebalanceDate = LocalDateTime.now();

    public static PortfolioRebalance create(Long customerId, RebalanceType rebalanceType, String soldTicker, String boughtTicker, Money soldAmount) {
        return PortfolioRebalance.builder()
                .id(null)
                .customerId(customerId)
                .rebalanceType(rebalanceType)
                .soldTicker(soldTicker)
                .boughtTicker(boughtTicker)
                .soldAmount(soldAmount)
                .rebalanceDate(LocalDateTime.now())
                .build();
    }

    public static PortfolioRebalance reconstruct(Long id, Long customerId, RebalanceType rebalanceType, String soldTicker, String boughtTicker, Money soldAmount, LocalDateTime rebalanceDate) {
        return PortfolioRebalance.builder()
                .id(id)
                .customerId(customerId)
                .rebalanceType(rebalanceType)
                .soldTicker(soldTicker)
                .boughtTicker(boughtTicker)
                .soldAmount(soldAmount)
                .rebalanceDate(rebalanceDate)
                .build();
    }
}
