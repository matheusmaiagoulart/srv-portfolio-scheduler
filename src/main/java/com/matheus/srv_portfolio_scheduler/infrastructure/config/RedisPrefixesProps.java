package com.matheus.srv_portfolio_scheduler.infrastructure.config;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Component
@ConfigurationProperties(prefix = "spring.data.redis.prefixes")
public class RedisPrefixesProps {
    private String customerPortfolio;
    private String portfolioHistory;

    public String getCustomerPortfolioPrefix() {
        return customerPortfolio;
    }

    public String getPortfolioHistoryPrefix() {
        return portfolioHistory;
    }
}
