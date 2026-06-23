package com.matheus.srv_portfolio_scheduler.infrastructure.entities;

import com.matheus.srv_portfolio_scheduler.domain.enums.BrokerageAccountType;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@Table(name = "brokerage_accounts")
@Entity(name = "brokerage_account")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JpaBrokerageAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id",  referencedColumnName = "id")
    private JpaCustomer customer;

    private String accountNumber;
    private BrokerageAccountType accountType;
    private OffsetDateTime createdAt;

    @Builder.Default
    @OneToMany(
            mappedBy = "brokerageAccount",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<JpaCustody> custodies = new ArrayList<>();

    public void addCustody(JpaCustody custody) {
        custodies.add(custody);
    }
}