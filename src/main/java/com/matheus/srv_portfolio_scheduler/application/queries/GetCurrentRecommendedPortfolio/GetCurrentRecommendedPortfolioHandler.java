package com.matheus.srv_portfolio_scheduler.application.queries.GetCurrentRecommendedPortfolio;

import com.matheus.srv_portfolio_scheduler.application.ports.input.queries.GetCurrentRecommendedPortfolioUseCase;
import com.matheus.srv_portfolio_scheduler.application.ports.output.queries.RecommendedPortfolioQueryRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetCurrentRecommendedPortfolioHandler implements GetCurrentRecommendedPortfolioUseCase {

    private final RecommendedPortfolioQueryRepositoryPort recommendedPortfolioQueryRepositoryPort;

    @Override
    public GetCurrentRecommendedPortfolioResponse handler(GetCurrentRecommendedPortfolioQuery query) {
        log.info("Executing GetCurrentRecommendedPortfolioQuery");
        return recommendedPortfolioQueryRepositoryPort.getCurrentRecommendedPortfolio();
    }
}
