package com.matheus.srv_portfolio_scheduler.application.command.CreateRecommendedPortfolio;

import com.matheus.srv_portfolio_scheduler.application.ports.input.CreateRecommendedPortfolioUseCase;
import com.matheus.srv_portfolio_scheduler.application.ports.output.RecommendedPortfolioRepositoryPort;
import com.matheus.srv_portfolio_scheduler.domain.entities.RecommendedPortfolio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateRecommendedPortfolioHandler implements CreateRecommendedPortfolioUseCase {

    private final RecommendedPortfolioRepositoryPort recommendedPortfolioRepository;

    @Override
    @Transactional
    public CreateRecommendedPortfolioResponse handler(CreateRecommendedPortfolioCommand request) {
        boolean rebalancingActivated = false;

        recommendedPortfolioRepository.getActiveRecommendedPortfolio().
                ifPresent(portfolio -> {
                    portfolio.deactivate();
                    recommendedPortfolioRepository.save(portfolio);
                });

        RecommendedPortfolio portfolio = RecommendedPortfolio.create(
                request.name(),
                request.portfolioItems().stream().map(PortfolioItemCommand::toDomain).toList());

        recommendedPortfolioRepository.save(portfolio);

        return CreateRecommendedPortfolioResponse.successfullyCreated(portfolio, rebalancingActivated);
    }
}
