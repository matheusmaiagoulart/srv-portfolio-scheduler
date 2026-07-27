package com.matheus.srv_portfolio_scheduler.application.command.RegisterCustomerSubscriber;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.matheus.srv_portfolio_scheduler.domain.entities.BrokerageAccount;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;

public record RegisterCustomerSubscriberResponse(
        Long id,
        String name,
        String message,
        Money monthlyAmount,
        @JsonProperty("BrokerageAccount")
        BrokerageAccountResponse brokerageAccountResponse
) {
    private static String getMessage(String name) {
        return "Congrats, " + name + "! Your subscription has been successfully registered.";
    }

    public static RegisterCustomerSubscriberResponse successRegister(BrokerageAccount brokerageAccount) {
        return new RegisterCustomerSubscriberResponse(
                brokerageAccount.getId(),
                brokerageAccount.getCustomer().getName(),
                getMessage(brokerageAccount.getCustomer().getName()),
                brokerageAccount.getCustomer().getMonthlyAmount(),

                BrokerageAccountResponse.builder()
                        .id(brokerageAccount.getId())
                        .accountNumber(brokerageAccount.getAccountNumber())
                        .accountType(brokerageAccount.getAccountType().toString())
                        .createdAt(brokerageAccount.getCreatedAt())
                        .build());
    }
}
