package com.matheus.srv_portfolio_scheduler.application.ports.output.commands;

import com.matheus.srv_portfolio_scheduler.domain.entities.BrokerageAccount;

import java.util.List;
import java.util.Optional;

public interface BrokerageAccountRepositoryPort {
    BrokerageAccount save(BrokerageAccount brokerageAccount);
    void saveAll(List<BrokerageAccount> brokerageAccounts);
    Optional<BrokerageAccount> getMasterAccount();
}
