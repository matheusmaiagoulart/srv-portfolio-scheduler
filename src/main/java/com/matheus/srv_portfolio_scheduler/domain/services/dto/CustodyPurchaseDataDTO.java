package com.matheus.srv_portfolio_scheduler.domain.services.dto;

import com.matheus.srv_portfolio_scheduler.domain.entities.BrokerageAccount;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import lombok.Getter;

public record CustodyPurchaseDataDTO(
        long customerId,
        String fullName,
        long brokerageAccountId,
        Money thirdPartyBalance,
        @Getter
        BrokerageAccount customerCustodies
) {
}
