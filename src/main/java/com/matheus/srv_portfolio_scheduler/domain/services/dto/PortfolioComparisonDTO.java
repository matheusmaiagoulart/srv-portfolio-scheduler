package com.matheus.srv_portfolio_scheduler.domain.services.dto;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;

public record PortfolioComparisonDTO(
        List<AlteredItem> altered,
        List<RemovedItem> removed,
        List<NewItem> added
) {
    public boolean hasChange() {
        return !altered.isEmpty() || !removed.isEmpty() || !added.isEmpty();
    }

    public record AlteredItem(String ticker, BigDecimal oldPercentage, BigDecimal newPercentage) {

        public boolean isIncrease() {
            return newPercentage.compareTo(oldPercentage) > 0;
        }
    }

    public record RemovedItem(String ticker) {
    }

    public record NewItem(String ticker, BigDecimal percentage) {
    }

    public HashSet<String> getAllTickersName() {
        HashSet<String> tickerNames = new HashSet<>();

        altered.forEach(item -> tickerNames.add(item.ticker()));
        removed.forEach(item -> tickerNames.add(item.ticker()));
        added.forEach(item -> tickerNames.add(item.ticker()));

        return tickerNames;
    }
}
