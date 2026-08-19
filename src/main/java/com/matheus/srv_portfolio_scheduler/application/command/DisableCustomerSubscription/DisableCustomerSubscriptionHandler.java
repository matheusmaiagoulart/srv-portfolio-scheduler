package com.matheus.srv_portfolio_scheduler.application.command.DisableCustomerSubscription;

import com.matheus.srv_portfolio_scheduler.application.utils.CorrelationId;
import com.matheus.srv_portfolio_scheduler.application.ports.input.commands.DisableCustomerSubscriptionUseCase;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.CustomerRepositoryPort;
import com.matheus.srv_portfolio_scheduler.domain.entities.Customer;
import com.matheus.srv_portfolio_scheduler.domain.exceptions.CustomerNotFound;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Service
@RequiredArgsConstructor
public class DisableCustomerSubscriptionHandler implements DisableCustomerSubscriptionUseCase {

    private final CustomerRepositoryPort customerRepository;

    @Override
    @Transactional
    public DisableCustomerSubscriptionResponse handler(DisableCustomerSubscriptionCommand command) {
        log.info("Starting customer subscription disable",
                kv("correlationId", CorrelationId.get()),
                kv("event", "DISABLE_CUSTOMER"),
                kv("customerId", command.customerId()));

        Customer customer = customerRepository.findById(command.customerId())
                .orElseThrow(() -> new CustomerNotFound(command.customerId()));

        customer.disable();
        customerRepository.save(customer);

        log.info("Customer subscription disabled successfully",
                kv("correlationId", CorrelationId.get()),
                kv("event", "DISABLE_CUSTOMER"),
                kv("customerId", command.customerId()));

        return DisableCustomerSubscriptionResponse.success(command.customerId());
    }
}

