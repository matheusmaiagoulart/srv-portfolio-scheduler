package com.matheus.srv_portfolio_scheduler.application.ports.output;

import com.matheus.srv_portfolio_scheduler.domain.entities.BrokerageAccount;

public interface BrokerageAccountRepositoryPort {
    BrokerageAccount save(BrokerageAccount brokerageAccount);
}
