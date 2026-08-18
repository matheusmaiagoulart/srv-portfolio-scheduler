package com.matheus.srv_portfolio_scheduler.domain.services.rebalance;

import com.matheus.srv_portfolio_scheduler.domain.entities.Custody;
import com.matheus.srv_portfolio_scheduler.domain.entities.PortfolioRebalance;
import com.matheus.srv_portfolio_scheduler.domain.enums.RebalanceType;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.RebalanceResultDTO;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SellRemovedAssetsOperation {

    public RebalanceResultDTO execute(
            Map<String, Custody> custodies,
            Set<String> removedTickers,
            Map<String, Money> pricesByTicker,
            Long customerId) {

        Money totalReleasedAmount = Money.create(BigDecimal.ZERO);
        List<PortfolioRebalance> portfolioRebalances = new ArrayList<>();

        for (String ticker : removedTickers) {
            Custody custody = custodies.get(ticker);
            if (custody == null || custody.getQuantity() == 0) continue;

            Money price = pricesByTicker.get(ticker);
            if (price == null) continue;

            // Vende TODA a posição do ativo removido, zerando a custódia
            Money soldAmount = custody.sell(custody.getQuantity(), price);
            totalReleasedAmount = totalReleasedAmount.add(soldAmount);

            portfolioRebalances.add(PortfolioRebalance.create(
                    customerId,
                    RebalanceType.PORTFOLIO_CHANGE,
                    ticker,
                    String.valueOf(""),
                    soldAmount));
        }

        return new RebalanceResultDTO(totalReleasedAmount, portfolioRebalances);
    }
}
