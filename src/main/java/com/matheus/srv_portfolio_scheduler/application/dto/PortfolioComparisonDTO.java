package com.matheus.srv_portfolio_scheduler.application.dto;

import java.math.BigDecimal;
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

        public BigDecimal percentageDifference() {
            return newPercentage.subtract(oldPercentage);
        }
    }

    public record RemovedItem(String ticker) {
    }

    public record NewItem(String ticker, BigDecimal percentage) {
    }
}
