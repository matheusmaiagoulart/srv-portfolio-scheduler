package com.matheus.srv_portfolio_scheduler.adapters.output.queries;

import com.matheus.srv_portfolio_scheduler.adapters.mapper.RecommendedPortfolioMapper;
import com.matheus.srv_portfolio_scheduler.application.ports.output.queries.RecommendedPortfolioQueryRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.queries.GetAllRecommendedPortfolios.GetAllRecommendedPortfoliosResponse;
import com.matheus.srv_portfolio_scheduler.application.queries.GetCurrentRecommendedPortfolio.GetCurrentRecommendedPortfolioResponse;
import com.matheus.srv_portfolio_scheduler.domain.entities.RecommendedPortfolio;
import com.matheus.srv_portfolio_scheduler.domain.exceptions.ActivePortfolioNotFoundException;
import com.matheus.srv_portfolio_scheduler.domain.exceptions.LatestTradingDateNotFoundException;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import com.matheus.srv_portfolio_scheduler.infrastructure.entities.JpaAssetPrice;
import com.matheus.srv_portfolio_scheduler.infrastructure.entities.JpaRecommendedPortfolio;
import com.matheus.srv_portfolio_scheduler.infrastructure.queries.JpaAssetPriceQueryRepository;
import com.matheus.srv_portfolio_scheduler.infrastructure.queries.JpaRecommendedPortfolioQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RecommendedPortfolioQueryRepositoryAdapter implements RecommendedPortfolioQueryRepositoryPort {

    private final JpaRecommendedPortfolioQueryRepository repository;
    private final JpaAssetPriceQueryRepository assetPriceRepository;

    @Override
    public GetAllRecommendedPortfoliosResponse getAllRecommendedPortfolios() {

        List<RecommendedPortfolio> portfolios = repository.findAll().stream()
                .map(RecommendedPortfolioMapper::toDomain)
                .toList();

        List<GetAllRecommendedPortfoliosResponse.RecommendedPortfolioDTO> portfoliosMapped = portfolios.stream()
                .map(entity -> new GetAllRecommendedPortfoliosResponse.RecommendedPortfolioDTO(
                        entity.getId(),
                        entity.getName(),
                        entity.isActive(),
                        entity.getCreatedAt(),
                        entity.getTerminationDate(),

                        entity.getPortfolioItems().stream()
                                .map(item -> new GetAllRecommendedPortfoliosResponse.ItemsDTO(
                                        item.getTicker(),
                                        item.getPercentage())).toList()

                )).toList();

        return new GetAllRecommendedPortfoliosResponse(portfoliosMapped);
    }

    @Override
    public GetCurrentRecommendedPortfolioResponse getCurrentRecommendedPortfolio() {
        JpaRecommendedPortfolio currentPortfolio = repository.getJpaRecommendedPortfolioByActiveTrue()
                .orElseThrow(ActivePortfolioNotFoundException::new);

        LocalDate latestTradingDate = assetPriceRepository.getLatestTradingDate()
                .orElseThrow(LatestTradingDateNotFoundException::new);

        Map<String, BigDecimal> assetsPrices = assetPriceRepository.getJpaAssetPriceByTradingDate(latestTradingDate)
                .stream()
                .collect(Collectors.toMap(
                        JpaAssetPrice::getTicker,
                        jpaAssetPrice -> jpaAssetPrice.getClosePrice().getAmount()));

        return new GetCurrentRecommendedPortfolioResponse(
                currentPortfolio.getId(),
                currentPortfolio.getName(),
                currentPortfolio.isActive(),
                currentPortfolio.getCreatedAt(),
                currentPortfolio.getPortfolioItems().stream()
                        .map(item -> new GetCurrentRecommendedPortfolioResponse.ItemsResponse(
                                item.getTicker(),
                                BigDecimal.valueOf(item.getPercentage()),
                                Money.create(assetsPrices.get(item.getTicker()))
                        )).toList()
        );
    }
}
