package com.matheus.srv_portfolio_scheduler.fixtures;

import com.matheus.srv_portfolio_scheduler.domain.entities.BrokerageAccount;
import com.matheus.srv_portfolio_scheduler.domain.entities.Customer;
import com.matheus.srv_portfolio_scheduler.domain.enums.BrokerageAccountType;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;

import java.time.OffsetDateTime;
import java.util.ArrayList;

public final class CustomerAggregateFixture {

    private Long customerId = TestDefaults.CUSTOMER_ID;
    private Long brokerageAccountId = TestDefaults.BROKERAGE_ACCOUNT_ID;
    private String name = TestDefaults.CUSTOMER_NAME;
    private String cpf = TestDefaults.CPF;
    private String email = TestDefaults.EMAIL;
    private Money monthlyAmount = Money.create(TestDefaults.MONTHLY_AMOUNT);
    private boolean active = true;
    private OffsetDateTime joiningDate = TestDefaults.OFFSET_DATE_TIME;
    private String accountNumber = TestDefaults.ACCOUNT_NUMBER;
    private BrokerageAccountType accountType = BrokerageAccountType.CLIENT;
    private OffsetDateTime accountCreatedAt = TestDefaults.OFFSET_DATE_TIME;

    private CustomerAggregateFixture() {
    }

    public static CustomerAggregateFixture aCustomerAggregate() {
        return new CustomerAggregateFixture();
    }

    public CustomerAggregateFixture withCustomerId(long customerId) {
        this.customerId = customerId;
        return this;
    }

    public CustomerAggregateFixture withBrokerageAccountId(long brokerageAccountId) {
        this.brokerageAccountId = brokerageAccountId;
        return this;
    }

    public CustomerAggregateFixture withName(String name) {
        this.name = name;
        return this;
    }

    public CustomerAggregateFixture withCpf(String cpf) {
        this.cpf = cpf;
        return this;
    }

    public CustomerAggregateFixture withEmail(String email) {
        this.email = email;
        return this;
    }

    public CustomerAggregateFixture withMonthlyAmount(String monthlyAmount) {
        this.monthlyAmount = TestDefaults.money(monthlyAmount);
        return this;
    }

    public CustomerAggregateFixture inactive() {
        this.active = false;
        return this;
    }

    public CustomerAggregateFixture withAccountType(BrokerageAccountType accountType) {
        this.accountType = accountType;
        return this;
    }

    public Customer build() {
        Customer customer = Customer.reconstruct(
                customerId,
                name,
                cpf,
                email,
                monthlyAmount,
                active,
                joiningDate);

        BrokerageAccount account = BrokerageAccount.reconstruct(
                brokerageAccountId,
                customer,
                accountNumber,
                accountType,
                accountCreatedAt,
                new ArrayList<>());

        customer.setBrokerageAccount(account);
        return customer;
    }
}