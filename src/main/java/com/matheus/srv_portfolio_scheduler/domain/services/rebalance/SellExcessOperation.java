package com.matheus.srv_portfolio_scheduler.domain.services.rebalance;

import com.matheus.srv_portfolio_scheduler.domain.entities.Custody;
import com.matheus.srv_portfolio_scheduler.domain.entities.PortfolioRebalance;
import com.matheus.srv_portfolio_scheduler.domain.enums.RebalanceType;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.RebalanceResultDTO;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SellExcessOperation {

    public RebalanceResultDTO execute(
            Map<String, Custody> custodies,
            Map<String, BigDecimal> alteredAssets,
            Map<String, Money> pricesByTicker,
            Long customerId,
            Money originalPortfolioValue) {

        Money totalReleasedAmount = Money.create(BigDecimal.ZERO);
        List<PortfolioRebalance> portfolioRebalances = new ArrayList<>();

        for (Map.Entry<String, BigDecimal> entry : alteredAssets.entrySet()) {

            String ticker = entry.getKey();
            BigDecimal newPercentage = entry.getValue();

            Custody custody = custodies.get(ticker);
            Money price = pricesByTicker.get(ticker);
            if (custody == null || price == null) continue;

            Money targetPortfolioValue = originalPortfolioValue.multiply(
                    newPercentage.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
            int targetQuantity = targetPortfolioValue.divide(price.getAmount()).getAmount().intValue();

            int quantityExcess = custody.getQuantity() - targetQuantity;

            if (quantityExcess > 0) {
                Money releasedAmount = custody.sell(quantityExcess, price);
                totalReleasedAmount = totalReleasedAmount.add(releasedAmount);

                portfolioRebalances.add(PortfolioRebalance.create(
                        customerId,
                        RebalanceType.PORTFOLIO_CHANGE,
                        ticker,
                        ticker,
                        releasedAmount));
            }
        }
        return new RebalanceResultDTO(totalReleasedAmount, portfolioRebalances);
    }
}
