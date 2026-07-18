package com.matheus.srv_portfolio_scheduler.application.command.ImportQuotes;

import com.matheus.srv_portfolio_scheduler.application.dto.QuoteDTO;
import com.matheus.srv_portfolio_scheduler.application.ports.input.commands.ImportQuotesUseCase;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.AssetPriceRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.CotahistFilePort;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.RecommendedPortfolioRepositoryPort;
import com.matheus.srv_portfolio_scheduler.domain.entities.AssetPrice;
import com.matheus.srv_portfolio_scheduler.domain.entities.PortfolioItem;
import com.matheus.srv_portfolio_scheduler.domain.entities.RecommendedPortfolio;
import com.matheus.srv_portfolio_scheduler.domain.exceptions.ActivePortfolioNotFoundException;
import com.matheus.srv_portfolio_scheduler.domain.exceptions.BusinessException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ImportQuotesHandler implements ImportQuotesUseCase {

    private final CotahistFilePort cotahistFilePort;
    private final AssetPriceRepositoryPort assetPriceRepository;
    private final RecommendedPortfolioRepositoryPort recommendedPortfolioRepositoryPort;

    @Override
    public List<QuoteDTO> handler(ImportQuotesCommand importQuotesCommand) {

        log.info("Starting import cotahist quotes. Reference date: {}", importQuotesCommand.referenceDate());

        String cotahistPath = cotahistFilePort.existsCotahistFile(importQuotesCommand.referenceDate())
                .orElseThrow(() -> new BusinessException("COTAHIST_NOT_FOUND",
                        "Cotahist file not found for the reference date: " + importQuotesCommand.referenceDate()));

        RecommendedPortfolio portfolio = recommendedPortfolioRepositoryPort.getActiveRecommendedPortfolio()
                .orElseThrow(ActivePortfolioNotFoundException::new);

        HashSet<String> tickers = portfolio.getPortfolioItems().stream()
                .map(PortfolioItem::getTicker)
                .collect(HashSet::new, HashSet::add, HashSet::addAll);

        List<QuoteDTO> cotahistResult = cotahistFilePort.parse(tickers, cotahistPath);

        List<AssetPrice> assetsPrices = cotahistResult.stream()
                .map(quote -> AssetPrice.create(
                        quote.dataPregao(),
                        quote.ticker(),
                        quote.openPrice(),
                        quote.closePrice(),
                        quote.maxPrice(),
                        quote.minPrice()))
                .toList();

        assetPriceRepository.saveAll(assetsPrices);
        return cotahistResult;
    }
}
