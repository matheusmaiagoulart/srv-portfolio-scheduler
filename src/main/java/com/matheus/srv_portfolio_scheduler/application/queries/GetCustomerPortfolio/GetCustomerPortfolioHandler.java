package com.matheus.srv_portfolio_scheduler.application.queries.GetCustomerPortfolio;

import com.matheus.srv_portfolio_scheduler.application.ports.input.GetCustomerPortfolioUseCase;
import com.matheus.srv_portfolio_scheduler.application.ports.output.queries.CustomerQueryRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetCustomerPortfolioHandler implements GetCustomerPortfolioUseCase {

    private final CustomerQueryRepositoryPort customerQueryRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public GetCustomerPortfolioResponse handler(GetCustomerPortfolioQuery query) {
        log.info("Handling GetCustomerPortfolioQuery for customerId: {}", query.customerId());
        return customerQueryRepositoryPort.getCustomerPortfolio(query.customerId());
    }
}
