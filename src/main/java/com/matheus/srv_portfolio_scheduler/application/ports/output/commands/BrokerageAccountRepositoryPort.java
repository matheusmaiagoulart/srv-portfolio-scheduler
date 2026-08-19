package com.matheus.srv_portfolio_scheduler.application.ports.output.commands;

import com.matheus.srv_portfolio_scheduler.domain.entities.BrokerageAccount;

import java.util.List;

public interface BrokerageAccountRepositoryPort {
    BrokerageAccount save(BrokerageAccount brokerageAccount);
    void saveAll(List<BrokerageAccount> brokerageAccounts);
    BrokerageAccount getMasterAccount();
}
