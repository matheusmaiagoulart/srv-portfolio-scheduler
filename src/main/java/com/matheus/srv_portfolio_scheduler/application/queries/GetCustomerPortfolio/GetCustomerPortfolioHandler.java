package com.matheus.srv_portfolio_scheduler.application.queries.GetCustomerPortfolio;

import com.matheus.srv_portfolio_scheduler.application.ports.input.queries.GetCustomerPortfolioUseCase;
import com.matheus.srv_portfolio_scheduler.application.ports.output.queries.RedisCachePort;
import com.matheus.srv_portfolio_scheduler.application.ports.output.queries.CustomerQueryRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetCustomerPortfolioHandler implements GetCustomerPortfolioUseCase {

    @Value("${spring.data.redis.prefixes.customer-portfolio}")
    private String CACHE_PREFIX;

    private final RedisCachePort redisCachePort;
    private final CustomerQueryRepositoryPort customerQueryRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public GetCustomerPortfolioResponse handler(GetCustomerPortfolioQuery query) {
        log.info("Handling GetCustomerPortfolioQuery for customerId: {}", query.customerId());

        final var queryConcat = CACHE_PREFIX.concat(String.valueOf(query.customerId()));

        var cached = redisCachePort.get(queryConcat, GetCustomerPortfolioResponse.class);
        if (cached.isPresent()) return cached.get();

        log.info("Executing query on database for customerId: {}", query.customerId());
        var response = customerQueryRepositoryPort.getCustomerPortfolio(query.customerId());
        redisCachePort.save(queryConcat, response, 250);
        return response;
    }
}
