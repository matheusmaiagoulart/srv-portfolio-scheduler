package com.matheus.srv_portfolio_scheduler.application.ports.output;

import com.matheus.srv_portfolio_scheduler.domain.entities.Customer;

public interface CustomerRepositoryPort {
    void save(Customer customer);
}
