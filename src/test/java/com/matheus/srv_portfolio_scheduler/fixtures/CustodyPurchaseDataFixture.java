package com.matheus.srv_portfolio_scheduler.fixtures;

import com.matheus.srv_portfolio_scheduler.domain.entities.BrokerageAccount;
import com.matheus.srv_portfolio_scheduler.domain.entities.Customer;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.CustodyPurchaseDataDTO;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class CustodyPurchaseDataFixture {

    private Customer customer = CustomerAggregateFixture.aCustomerAggregate().build();
    private Money thirdPartyBalance = Money.create(TestDefaults.THIRD_PARTY_BALANCE);

    private CustodyPurchaseDataFixture() {
    }

    public static CustodyPurchaseDataFixture aCustodyPurchaseData() {
        return new CustodyPurchaseDataFixture();
    }

    public CustodyPurchaseDataFixture fromCustomer(Customer customer) {
        this.customer = customer;
        return this;
    }

    public CustodyPurchaseDataFixture withThirdPartyBalance(String thirdPartyBalance) {
        this.thirdPartyBalance = TestDefaults.money(thirdPartyBalance);
        return this;
    }

    public CustodyPurchaseDataDTO build() {
        BrokerageAccount account = customer.getBrokerageAccount();

        if (account == null) {
            throw new IllegalStateException("Customer must have a brokerage account");
        }

        return new CustodyPurchaseDataDTO(
                customer.getId(),
                customer.getName(),
                account.getId(),
                thirdPartyBalance,
                account);
    }

    public Map<Long, CustodyPurchaseDataDTO> asMap() {
        CustodyPurchaseDataDTO data = build();
        return Map.of(data.brokerageAccountId(), data);
    }

    public static Map<Long, CustodyPurchaseDataDTO> asMap(CustodyPurchaseDataDTO... customers) {
        return Arrays.stream(customers)
                .collect(Collectors.toMap(
                        CustodyPurchaseDataDTO::brokerageAccountId,
                        Function.identity()));
    }
}