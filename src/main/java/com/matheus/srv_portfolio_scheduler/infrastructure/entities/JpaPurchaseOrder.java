package com.matheus.srv_portfolio_scheduler.infrastructure.entities;

import com.matheus.srv_portfolio_scheduler.domain.enums.MarketType;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@Table(name = "purchase_orders")
@Entity(name = "purchase_order")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JpaPurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long masterAccountId;
    private String ticker;
    private int quantity;
    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "unit_price"))
    private Money unitPrice;
    private MarketType marketType;
    private OffsetDateTime executionDate;

    @Builder.Default
    @OneToMany(
            mappedBy = "purchaseOrder",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    List<JpaDelivery> deliveries = new ArrayList<>();
}
