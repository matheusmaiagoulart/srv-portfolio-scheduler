package com.matheus.srv_portfolio_scheduler.domain.entities;

import com.matheus.srv_portfolio_scheduler.domain.exceptions.BusinessException;
import com.matheus.srv_portfolio_scheduler.domain.exceptions.CustomerAlreadyInactiveException;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Customer {

    private Long id;
    private String name;
    private String cpf;
    private String email;
    private Money monthlyAmount;
    private boolean active;
    private OffsetDateTime joiningDate;

    @Setter
    private BrokerageAccount brokerageAccount;


    public static Customer create(String name, String cpf, String email, BigDecimal monthlyAmount) {
        if (name == null || name.isBlank()) throw new BusinessException("INVALID_NAME", "Name can't be null.");
        if (cpf == null || cpf.isBlank()) throw new BusinessException("INVALID_CPF", "Cpf can't be null.");
        if (email == null || email.isBlank()) throw new BusinessException("INVALID_EMAIL", "Email can't be null.");

        if (monthlyAmount == null || monthlyAmount.compareTo(BigDecimal.valueOf(100)) < 0)
            throw new BusinessException("INVALID_MONTHLY_AMOUNT", "Monthly amount must be greater than 100.");

        Customer customer = Customer.builder()
                .id(null)
                .name(name)
                .cpf(cpf)
                .email(email)
                .monthlyAmount(Money.create(monthlyAmount))
                .active(true)
                .joiningDate(OffsetDateTime.now())
                .build();

        customer.brokerageAccount = BrokerageAccount.create(customer);

        return customer;
    }

    public void disable() {
        if (!this.active) throw new CustomerAlreadyInactiveException(this.id);
        this.active = false;
    }

    public static Customer reconstruct(Long id, String name, String cpf, String email, Money monthlyAmount, boolean active, OffsetDateTime joiningDate) {
        return Customer.builder()
                .id(id)
                .name(name)
                .cpf(cpf)
                .email(email)
                .monthlyAmount(monthlyAmount)
                .active(active)
                .joiningDate(joiningDate)
                .build();
    }
}
