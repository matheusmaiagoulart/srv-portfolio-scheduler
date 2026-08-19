package com.matheus.srv_portfolio_scheduler.application.config;

import com.matheus.srv_portfolio_scheduler.domain.services.*;
import com.matheus.srv_portfolio_scheduler.domain.services.rebalance.CalculateBuyNeedOperation;
import com.matheus.srv_portfolio_scheduler.domain.services.rebalance.CustodyTickerMigrationOperation;
import com.matheus.srv_portfolio_scheduler.domain.services.rebalance.IndividualDistributionOperation;
import com.matheus.srv_portfolio_scheduler.domain.services.rebalance.SellExcessOperation;
import com.matheus.srv_portfolio_scheduler.domain.services.rebalance.SellRemovedAssetsOperation;
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

    @Bean
    public PortfolioComparisonService portfolioComparisonService() {
        return new PortfolioComparisonService();
    }

    @Bean
    public SellExcessOperation sellExcessOperation() {
        return new SellExcessOperation();
    }

    @Bean
    public SellRemovedAssetsOperation sellRemovedAssetsOperation() {
        return new SellRemovedAssetsOperation();
    }

    @Bean
    public CustodyTickerMigrationOperation custodyTickerMigrationOperation() {
        return new CustodyTickerMigrationOperation();
    }

    @Bean
    public CalculateBuyNeedOperation calculateBuyNeedOperation() {
        return new CalculateBuyNeedOperation();
    }

    @Bean
    public IndividualDistributionOperation individualDistributionOperation() {
        return new IndividualDistributionOperation();
    }
}
