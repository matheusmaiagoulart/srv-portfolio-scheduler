package com.matheus.srv_portfolio_scheduler.infrastructure.entities;

import com.matheus.srv_portfolio_scheduler.domain.enums.RebalanceType;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@Table(name = "portfolio_rebalances")
@Entity(name = "portfolio_rebalance")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JpaPortfolioRebalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long customerId;
    private RebalanceType rebalanceType;
    private String soldTicker;
    private String boughtTicker;
    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "sold_amount"))
    private Money soldAmount;
    private LocalDateTime rebalanceDate;
}
