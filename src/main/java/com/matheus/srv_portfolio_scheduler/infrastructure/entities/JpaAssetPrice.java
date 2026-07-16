package com.matheus.srv_portfolio_scheduler.infrastructure.entities;

import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Getter
@Builder
@Table(name = "asset_prices")
@Entity(name = "asset_prices")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JpaAssetPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private LocalDate TradingDate;
    private String ticker;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "open_price", precision = 18, scale = 4))
    private Money openPrice;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "close_price", precision = 18, scale = 4))
    private Money closePrice;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "max_price", precision = 18, scale = 4))
    private Money maxPrice;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "min_price", precision = 18, scale = 4))
    private Money minPrice;
}
