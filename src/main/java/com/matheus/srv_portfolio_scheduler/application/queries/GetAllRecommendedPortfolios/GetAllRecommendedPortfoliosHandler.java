package com.matheus.srv_portfolio_scheduler.application.queries.GetAllRecommendedPortfolios;

import com.matheus.srv_portfolio_scheduler.application.ports.input.queries.GetAllRecommendedPortfoliosUseCase;
import com.matheus.srv_portfolio_scheduler.application.ports.output.queries.RecommendedPortfolioQueryRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.ports.output.queries.RedisCachePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetAllRecommendedPortfoliosHandler implements GetAllRecommendedPortfoliosUseCase {

    private static final String CACHE_PREFIX = "portfolioHistory";

    private final RedisCachePort redisCachePort;
    private final RecommendedPortfolioQueryRepositoryPort recommendedPortfolioQueryRepositoryPort;

    @Override
    public GetAllRecommendedPortfoliosResponse handler(GetAllRecommendedPortfoliosQuery query) {
        log.info("Executing search for all recommended portfolios");

        var cached = redisCachePort.get(CACHE_PREFIX, GetAllRecommendedPortfoliosResponse.class);
        if (cached.isPresent()) return cached.get();

        var response = recommendedPortfolioQueryRepositoryPort.getAllRecommendedPortfolios();
        redisCachePort.save(CACHE_PREFIX, response, 250);
        return response;
    }
}
