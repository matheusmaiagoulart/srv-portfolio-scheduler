package com.matheus.srv_portfolio_scheduler.domain.services;

import com.matheus.srv_portfolio_scheduler.domain.services.dto.PortfolioComparisonDTO;
import com.matheus.srv_portfolio_scheduler.domain.entities.PortfolioItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PortfolioComparisonService {

    public PortfolioComparisonDTO compare (List<PortfolioItem> current, List<PortfolioItem> incoming) {

        Map<String, PortfolioItem> currentAssetsDictionary = current.stream()
                .collect(Collectors.toMap(PortfolioItem::getTicker, portfolioItem -> portfolioItem));

        Map<String, PortfolioItem> incomingAssetsDictionary = incoming.stream()
                .collect(Collectors.toMap(PortfolioItem::getTicker, portfolioItem -> portfolioItem));

        List<PortfolioComparisonDTO.RemovedItem> removedTickers = currentAssetsDictionary.keySet().stream()
                .filter(ticker -> !incomingAssetsDictionary.containsKey(ticker))
                .map(PortfolioComparisonDTO.RemovedItem::new)
                .toList();

        List<PortfolioComparisonDTO.NewItem> addedTickers = incomingAssetsDictionary.keySet().stream()
                .filter(ticker -> !currentAssetsDictionary.containsKey(ticker))
                .map(ticker -> new PortfolioComparisonDTO.NewItem(
                        ticker,
                        BigDecimal.valueOf(incomingAssetsDictionary.get(ticker).getPercentage())))
                .toList();

        List<PortfolioComparisonDTO.AlteredItem> alteredTickers = currentAssetsDictionary.keySet().stream()
                .filter(incomingAssetsDictionary::containsKey)
                .filter(ticker -> currentAssetsDictionary.get(ticker).getPercentage() != incomingAssetsDictionary.get(ticker).getPercentage())
                .map(ticker -> new PortfolioComparisonDTO.AlteredItem(
                        ticker,
                        BigDecimal.valueOf(currentAssetsDictionary.get(ticker).getPercentage()),
                        BigDecimal.valueOf(incomingAssetsDictionary.get(ticker).getPercentage())))
                .toList();

        return new PortfolioComparisonDTO(alteredTickers, removedTickers, addedTickers);

    }
}
