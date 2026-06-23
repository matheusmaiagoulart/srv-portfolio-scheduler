package com.matheus.srv_portfolio_scheduler.adapters.output;

import com.matheus.srv_portfolio_scheduler.adapters.mapper.CustomerMapper;
import com.matheus.srv_portfolio_scheduler.application.ports.output.CustomerRepositoryPort;
import com.matheus.srv_portfolio_scheduler.domain.entities.Customer;
import com.matheus.srv_portfolio_scheduler.infrastructure.persistence.JpaCustomerRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CustomerRepositoryAdapter implements CustomerRepositoryPort {

    private final JpaCustomerRepository jpaCustomerRepository;

    @Override
    public void save(Customer customer) {
        jpaCustomerRepository.save(CustomerMapper.toJpaEntity(customer));
    }
}
