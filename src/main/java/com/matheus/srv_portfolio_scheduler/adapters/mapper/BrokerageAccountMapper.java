package com.matheus.srv_portfolio_scheduler.adapters.mapper;

import com.matheus.srv_portfolio_scheduler.domain.entities.BrokerageAccount;
import com.matheus.srv_portfolio_scheduler.domain.entities.Custody;
import com.matheus.srv_portfolio_scheduler.domain.entities.Customer;
import com.matheus.srv_portfolio_scheduler.infrastructure.entities.JpaBrokerageAccount;
import com.matheus.srv_portfolio_scheduler.infrastructure.entities.JpaCustody;
import com.matheus.srv_portfolio_scheduler.infrastructure.entities.JpaCustomer;

import java.util.ArrayList;
import java.util.List;

public class BrokerageAccountMapper {

    public static JpaBrokerageAccount toJpaEntity(BrokerageAccount brokerageAccount, JpaCustomer customer) {
        JpaBrokerageAccount jpaEntity = JpaBrokerageAccount.builder()
                .id(brokerageAccount.getId())
                .customer(customer)
                .accountNumber(brokerageAccount.getAccountNumber())
                .accountType(brokerageAccount.getAccountType())
                .createdAt(brokerageAccount.getCreatedAt())
                .build();

        List<JpaCustody> custodies = brokerageAccount.getCustodies().stream()
                .map(custody -> CustodyMapper.toJpaEntity(custody, jpaEntity))
                .toList();

        custodies.forEach(jpaEntity::addCustody);

        return jpaEntity;
    }

    public static BrokerageAccount toDomain(JpaBrokerageAccount account, Customer customer) {
        BrokerageAccount domainEntity = BrokerageAccount.reconstruct(
                account.getId(),
                customer,
                account.getAccountNumber(),
                account.getAccountType(),
                account.getCreatedAt(),
                new ArrayList<>()
        );

        List<Custody> custodies = account.getCustodies().stream()
                .map(custody -> CustodyMapper.toDomain(custody, domainEntity)).toList();

        domainEntity.setCustodies(custodies);

        return domainEntity;
    }
}
