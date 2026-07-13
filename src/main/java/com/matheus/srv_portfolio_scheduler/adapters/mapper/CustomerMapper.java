package com.matheus.srv_portfolio_scheduler.adapters.mapper;

import com.matheus.srv_portfolio_scheduler.domain.entities.Customer;
import com.matheus.srv_portfolio_scheduler.infrastructure.entities.JpaCustomer;

public class CustomerMapper {

    public static JpaCustomer toJpaEntity(Customer customer) {
        return JpaCustomer.builder()
                .id(customer.getId())
                .name(customer.getName())
                .cpf(customer.getCpf())
                .email(customer.getEmail())
                .monthlyAmount(customer.getMonthlyAmount())
                .active(customer.isActive())
                .joiningDate(customer.getJoiningDate())
                .build();
    }

    public static Customer toDomain(JpaCustomer customer) {
        return Customer.reconstruct(
                customer.getId(),
                customer.getName(),
                customer.getCpf(),
                customer.getEmail(),
                customer.getMonthlyAmount(),
                customer.isActive(),
                customer.getJoiningDate()
        );
    }
}
