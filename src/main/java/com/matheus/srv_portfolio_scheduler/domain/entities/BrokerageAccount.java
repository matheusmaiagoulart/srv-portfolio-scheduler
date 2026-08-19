package com.matheus.srv_portfolio_scheduler.domain.entities;

import com.matheus.srv_portfolio_scheduler.domain.enums.BrokerageAccountType;
import com.matheus.srv_portfolio_scheduler.domain.exceptions.BusinessException;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BrokerageAccount {

    private Long id;

    private Customer customer;

    private String accountNumber;
    private BrokerageAccountType accountType;
    private OffsetDateTime createdAt;

    @Setter
    @Builder.Default
    private List<Custody> custodies = new ArrayList<>();

    public static BrokerageAccount create(Customer customer) {
        return BrokerageAccount.builder()
                .id(null)
                .customer(customer)
                .accountNumber(generateAccountNumber())
                .accountType(BrokerageAccountType.CLIENT)
                .createdAt(OffsetDateTime.now())
                .build();
    }

    private static String generateAccountNumber() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public void createInitialCustodies(List<PortfolioItem> portfolioItems) {
        if (portfolioItems == null || portfolioItems.size() != 5) {
            throw new BusinessException(
                    "INVALID_PORTFOLIO_ITEMS",
                    "O portfólio recomendado deve conter 5 items para criação das Custodias!");
        }

        this.custodies = portfolioItems
                .stream()
                .map(item -> Custody.create(this, item.getTicker()))
                .toList();
    }

    public static BrokerageAccount reconstruct(
            Long id, Customer customer, String accountNumber,
            BrokerageAccountType accountType, OffsetDateTime createdAt, List<Custody> custodies) {
        return BrokerageAccount.builder()
                .id(id)
                .customer(customer)
                .accountNumber(accountNumber)
                .accountType(accountType)
                .createdAt(createdAt)
                .custodies(custodies)
                .build();
    }

}


