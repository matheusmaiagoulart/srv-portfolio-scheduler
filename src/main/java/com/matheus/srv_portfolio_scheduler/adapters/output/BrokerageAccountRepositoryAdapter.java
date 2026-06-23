package com.matheus.srv_portfolio_scheduler.adapters.output;

import com.matheus.srv_portfolio_scheduler.adapters.mapper.BrokerageAccountMapper;
import com.matheus.srv_portfolio_scheduler.adapters.mapper.CustomerMapper;
import com.matheus.srv_portfolio_scheduler.adapters.utils.CorrelationId;
import com.matheus.srv_portfolio_scheduler.application.ports.output.BrokerageAccountRepositoryPort;
import com.matheus.srv_portfolio_scheduler.domain.entities.BrokerageAccount;
import com.matheus.srv_portfolio_scheduler.domain.entities.Customer;
import com.matheus.srv_portfolio_scheduler.domain.exceptions.BusinessException;
import com.matheus.srv_portfolio_scheduler.domain.exceptions.DuplicatedCpfException;
import com.matheus.srv_portfolio_scheduler.domain.exceptions.DuplicatedEmailException;
import com.matheus.srv_portfolio_scheduler.infrastructure.entities.JpaBrokerageAccount;
import com.matheus.srv_portfolio_scheduler.infrastructure.entities.JpaCustomer;
import com.matheus.srv_portfolio_scheduler.infrastructure.persistence.JpaBrokerageAccountRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Component
@AllArgsConstructor
public class BrokerageAccountRepositoryAdapter implements BrokerageAccountRepositoryPort {

    private JpaBrokerageAccountRepository repository;

    @Override
    public BrokerageAccount save(BrokerageAccount brokerageAccount) {

        JpaCustomer customer = CustomerMapper.toJpaEntity(brokerageAccount.getCustomer());
        JpaBrokerageAccount brokerageEntity = BrokerageAccountMapper.toJpaEntity(brokerageAccount, customer);

        try {
            JpaBrokerageAccount saveResult = repository.save(brokerageEntity);

            Customer customerDomain = CustomerMapper.toDomain(saveResult.getCustomer());

            return BrokerageAccountMapper.toDomain(saveResult, customerDomain);
        } catch (Exception e) {
            String message = e.getMessage();

            log.error("Error while saving BrokerageAccount",
                    kv("correlationId", CorrelationId.get()),
                    kv("errorMessage", message));

            if (message.contains("IX_customers_cpf")) throw new DuplicatedCpfException(customer.getCpf());
            if (message.contains("IX_customers_email")) throw new DuplicatedEmailException(customer.getEmail());
        }
        throw new BusinessException("DATABASE_ERROR", "An error occurred while saving the brokerage account.");
    }
}