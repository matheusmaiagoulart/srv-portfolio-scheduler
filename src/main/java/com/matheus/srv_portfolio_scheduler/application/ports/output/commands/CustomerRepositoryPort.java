package com.matheus.srv_portfolio_scheduler.application.ports.output.commands;

import com.matheus.srv_portfolio_scheduler.domain.entities.Customer;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.CustodyPurchaseDataDTO;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;

import java.util.Map;

public interface CustomerRepositoryPort {
    void save(Customer customer);

    Money getThirdAmountOfAllActiveCustomers();

    Map<Long, CustodyPurchaseDataDTO> getChunkOfCustomers(long lastId,  int batchSize);
}
