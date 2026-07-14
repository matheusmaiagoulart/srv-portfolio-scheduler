package com.matheus.srv_portfolio_scheduler.infrastructure.config;

import com.matheus.srv_portfolio_scheduler.domain.services.IRDedoDuroCalculator;
import com.matheus.srv_portfolio_scheduler.domain.services.PortfolioDistribution;
import com.matheus.srv_portfolio_scheduler.domain.services.PurchaseExecutionDateValidator;
import com.matheus.srv_portfolio_scheduler.domain.services.PurchaseQuotesCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainServicesConfiguration {

    @Bean
    public PurchaseExecutionDateValidator purchaseExecutionDateValidator() {
        return new PurchaseExecutionDateValidator();
    }

    @Bean
    public PurchaseQuotesCalculator purchaseQuotesCalculator() {
        return new PurchaseQuotesCalculator();
    }

    @Bean
    public PortfolioDistribution portfolioDistribution() {
        return new PortfolioDistribution();
    }

    @Bean
    public IRDedoDuroCalculator irDedoDuroCalculator() {
        return new IRDedoDuroCalculator();
    }
}
