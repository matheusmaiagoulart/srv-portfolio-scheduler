package com.matheus.srv_portfolio_scheduler.application.command.CreateRecommendedPortfolio;

import com.matheus.srv_portfolio_scheduler.application.utils.CorrelationId;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.PortfolioComparisonDTO;
import com.matheus.srv_portfolio_scheduler.application.event.PortfolioRebalanceRequestedEvent;
import com.matheus.srv_portfolio_scheduler.application.ports.input.commands.CreateRecommendedPortfolioUseCase;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.RecommendedPortfolioRepositoryPort;
import com.matheus.srv_portfolio_scheduler.domain.entities.RecommendedPortfolio;
import com.matheus.srv_portfolio_scheduler.domain.services.PortfolioComparisonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateRecommendedPortfolioHandler implements CreateRecommendedPortfolioUseCase {

    private final PortfolioComparisonService portfolioComparisonService;
    private final RecommendedPortfolioRepositoryPort recommendedPortfolioRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public CreateRecommendedPortfolioResponse handler(CreateRecommendedPortfolioCommand request) {
        boolean rebalancingActivated = false;

        log.info("Creating recommended portfolio", kv("correlationId", CorrelationId.get()));

        RecommendedPortfolio currentPortfolio = recommendedPortfolioRepository
                .getActiveRecommendedPortfolio()
                .orElse(null);

        if (currentPortfolio != null) {
            currentPortfolio.deactivate();
            recommendedPortfolioRepository.save(currentPortfolio);
        }

        RecommendedPortfolio portfolio = RecommendedPortfolio.create(
                request.name(),
                request.portfolioItems().stream().map(PortfolioItemCommand::toDomain).toList(),
                request.terminationDate());

        recommendedPortfolioRepository.save(portfolio);
        log.info("Recommended portfolio created", kv("correlationId", CorrelationId.get()));

        if (currentPortfolio != null) {
            PortfolioComparisonDTO result = portfolioComparisonService.compare(currentPortfolio.getPortfolioItems(), portfolio.getPortfolioItems());

            if (result.hasChange()) {
                rebalancingActivated = true;
                eventPublisher.publishEvent(new PortfolioRebalanceRequestedEvent(result));
            }
        }

        return CreateRecommendedPortfolioResponse.successfullyCreated(portfolio, rebalancingActivated);
    }
}
