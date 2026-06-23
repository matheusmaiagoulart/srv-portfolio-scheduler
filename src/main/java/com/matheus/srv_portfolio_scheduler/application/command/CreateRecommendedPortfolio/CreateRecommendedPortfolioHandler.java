package com.matheus.srv_portfolio_scheduler.application.command.CreateRecommendedPortfolio;

import com.matheus.srv_portfolio_scheduler.adapters.utils.CorrelationId;
import com.matheus.srv_portfolio_scheduler.application.ports.input.CreateRecommendedPortfolioUseCase;
import com.matheus.srv_portfolio_scheduler.application.ports.output.RecommendedPortfolioRepositoryPort;
import com.matheus.srv_portfolio_scheduler.domain.entities.RecommendedPortfolio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateRecommendedPortfolioHandler implements CreateRecommendedPortfolioUseCase {

    private final RecommendedPortfolioRepositoryPort recommendedPortfolioRepository;

    @Override
    @Transactional
    public CreateRecommendedPortfolioResponse handler(CreateRecommendedPortfolioCommand request) {
        boolean rebalancingActivated = false;

        log.info("Creating recommended portfolio", kv("correlationId", CorrelationId.get()));

        recommendedPortfolioRepository.getActiveRecommendedPortfolio().
                ifPresent(portfolio -> {
                    portfolio.deactivate();
                    recommendedPortfolioRepository.save(portfolio);
                });

        RecommendedPortfolio portfolio = RecommendedPortfolio.create(
                request.name(),
                request.portfolioItems().stream().map(PortfolioItemCommand::toDomain).toList(),
                request.terminationDate());

        recommendedPortfolioRepository.save(portfolio);
        log.info("Recommended portfolio created", kv("correlationId", CorrelationId.get()));

        return CreateRecommendedPortfolioResponse.successfullyCreated(portfolio, rebalancingActivated);
    }
}
