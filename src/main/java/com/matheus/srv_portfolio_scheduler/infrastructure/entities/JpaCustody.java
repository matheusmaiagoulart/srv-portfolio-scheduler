package com.matheus.srv_portfolio_scheduler.infrastructure.entities;

import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Builder
@Table(name = "custodies")
@Entity(name = "custody")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JpaCustody {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brokerage_account_id")
    private JpaBrokerageAccount brokerageAccount;

    private String ticker;
    private int quantity;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "average_price"))
    public Money averagePrice;

    private OffsetDateTime lastUpdate;
}
