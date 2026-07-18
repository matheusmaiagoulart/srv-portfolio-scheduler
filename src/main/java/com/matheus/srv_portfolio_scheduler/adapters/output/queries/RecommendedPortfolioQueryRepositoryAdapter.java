package com.matheus.srv_portfolio_scheduler.adapters.output.queries;

import com.matheus.srv_portfolio_scheduler.adapters.mapper.RecommendedPortfolioMapper;
import com.matheus.srv_portfolio_scheduler.application.ports.output.queries.RecommendedPortfolioQueryRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.queries.GetAllRecommendedPortfolios.GetAllRecommendedPortfoliosResponse;
import com.matheus.srv_portfolio_scheduler.domain.entities.RecommendedPortfolio;
import com.matheus.srv_portfolio_scheduler.infrastructure.queries.JpaRecommendedPortfolioQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RecommendedPortfolioQueryRepositoryAdapter implements RecommendedPortfolioQueryRepositoryPort {
    private final JpaRecommendedPortfolioQueryRepository repository;

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
}