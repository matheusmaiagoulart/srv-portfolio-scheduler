package com.matheus.srv_portfolio_scheduler.infrastructure.entities;

import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Builder
@Entity(name = "customer")
@Table(name = "customers")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JpaCustomer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String cpf;

    private String email;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "monthly_amount"))
    private Money monthlyAmount;

    private boolean active;
    private OffsetDateTime joiningDate;

    @OneToOne(mappedBy = "customer")
    private JpaBrokerageAccount brokerageAccount;
}
