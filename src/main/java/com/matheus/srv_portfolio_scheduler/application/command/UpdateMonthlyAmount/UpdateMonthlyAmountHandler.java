package com.matheus.srv_portfolio_scheduler.application.command.UpdateMonthlyAmount;

import com.matheus.srv_portfolio_scheduler.adapters.utils.CorrelationId;
import com.matheus.srv_portfolio_scheduler.application.ports.input.commands.UpdateMonthlyAmountUseCase;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.CustomerRepositoryPort;
import com.matheus.srv_portfolio_scheduler.domain.entities.Customer;
import com.matheus.srv_portfolio_scheduler.domain.exceptions.CustomerNotFound;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateMonthlyAmountHandler implements UpdateMonthlyAmountUseCase {

    private final CustomerRepositoryPort customerRepository;

    @Override
    @Transactional
    public UpdateMonthlyAmountResponse handler(long customerId, UpdateMonthlyAmountCommand command) {
        log.info("Starting monthly amount update",
                kv("correlationId", CorrelationId.get()),
                kv("event", "UPDATE_MONTHLY_AMOUNT"),
                kv("customerId", customerId));

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFound(customerId));

        Money lastMonthlyAmount = customer.getMonthlyAmount();
        Money newMonthlyAmount = Money.create(BigDecimal.valueOf(command.newMonthlyAmount()));

        customer.updateMonthlyAmount(newMonthlyAmount);
        customerRepository.save(customer);

        log.info("Monthly amount updated successfully",
                kv("correlationId", CorrelationId.get()),
                kv("event", "UPDATE_MONTHLY_AMOUNT"),
                kv("customerId", customerId));

        return UpdateMonthlyAmountResponse.success(customerId, lastMonthlyAmount, newMonthlyAmount);
    }
}



