package com.matheus.srv_portfolio_scheduler.application.command.RegisterCustomerSubscriber;

import com.matheus.srv_portfolio_scheduler.application.ports.input.commands.RegisterCustomerSubscriberUseCase;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.BrokerageAccountRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.RecommendedPortfolioRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.utils.CorrelationId;
import com.matheus.srv_portfolio_scheduler.domain.entities.BrokerageAccount;
import com.matheus.srv_portfolio_scheduler.domain.entities.Customer;
import com.matheus.srv_portfolio_scheduler.domain.entities.RecommendedPortfolio;
import com.matheus.srv_portfolio_scheduler.domain.exceptions.ActivePortfolioNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterCustomerSubscriberHandler implements RegisterCustomerSubscriberUseCase {

    private final RecommendedPortfolioRepositoryPort recommendedPortfolioRepository;
    private final BrokerageAccountRepositoryPort brokerageAccountRepositoryPort;

    @Override
    @Transactional
    public RegisterCustomerSubscriberResponse handler(RegisterCustomerSubscriberCommand request) {
        log.info("Starting customer registration",
                kv("correlationId", CorrelationId.get()),
                kv("event", "REGISTER_CUSTOMER"),
                kv("email", request.email()));

        Customer customer = Customer.create(
                request.name(),
                request.cpf(),
                request.email(),
                request.monthlyAmount());

        RecommendedPortfolio activePortfolio = recommendedPortfolioRepository.getActiveRecommendedPortfolio()
                .orElseThrow(ActivePortfolioNotFoundException::new);

        log.info("Initializing custodies creation with base portfolio: {}", activePortfolio.getName(),
                kv("correlationId", CorrelationId.get()),
                kv("event", "REGISTER_CUSTOMER"));

        customer.getBrokerageAccount().createInitialCustodies(activePortfolio.getPortfolioItems());

        BrokerageAccount account = brokerageAccountRepositoryPort.save(customer.getBrokerageAccount());

        log.info("Customer was registered successfully",
                kv("correlationId", CorrelationId.get()),
                kv("event", "REGISTER_CUSTOMER"));

        return RegisterCustomerSubscriberResponse.successRegister(account);
    }
}
