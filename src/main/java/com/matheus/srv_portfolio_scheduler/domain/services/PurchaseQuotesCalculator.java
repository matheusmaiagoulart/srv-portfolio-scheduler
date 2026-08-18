package com.matheus.srv_portfolio_scheduler.domain.services;

import com.matheus.srv_portfolio_scheduler.domain.services.dto.QuoteDTO;
import com.matheus.srv_portfolio_scheduler.domain.entities.BrokerageAccount;
import com.matheus.srv_portfolio_scheduler.domain.entities.Custody;
import com.matheus.srv_portfolio_scheduler.domain.entities.RecommendedPortfolio;
import com.matheus.srv_portfolio_scheduler.domain.exceptions.QuoteNotFoundException;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.AssetPurchaseDTO;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class PurchaseQuotesCalculator {

    public Map<String, Money> calculateAmountPerAsset(Money totalAmount, RecommendedPortfolio portfolio) {
        Map<String, Money> amountPerAsset = new HashMap<>();

        portfolio.getPortfolioItems().forEach(item -> {
            BigDecimal percentage = BigDecimal.valueOf(item.getPercentage(), 2);
            Money assetAmount = Money.create((totalAmount.getAmount().multiply(percentage)));

            amountPerAsset.put(item.getTicker(), assetAmount);
        });

        return amountPerAsset;
    }

    public Map<String, AssetPurchaseDTO> calculateQuantityPerAsset(Map<String, Money> amountPerAsset, List<QuoteDTO> lastClosePricesCotahist,
            BrokerageAccount masterAccount) {

        Map<String, QuoteDTO> quotesByTicker = lastClosePricesCotahist.stream()
                .collect(Collectors.toMap(QuoteDTO::ticker, quote -> quote));

        Map<String, Integer> masterCustodies = masterAccount.getCustodies().stream()
                .collect(Collectors.toMap(Custody::getTicker, Custody::getQuantity));

        Map<String, AssetPurchaseDTO> quantityPerAssetResult = new HashMap<>();

        amountPerAsset.forEach((ticker, amount) -> {

            QuoteDTO tickerLoteQuote = Optional.of(quotesByTicker.get(ticker))
                    .orElseThrow(() -> new QuoteNotFoundException(ticker));

            QuoteDTO tickerFractionalQuote = Optional.of(quotesByTicker.get(ticker.concat("F")))
                    .orElseThrow(() -> new QuoteNotFoundException(ticker.concat("F")));

            Money lastClosePriceLote = Money.create(tickerLoteQuote.closePrice());
            Money lastClosePriceFractional = Money.create(tickerFractionalQuote.closePrice());

            int quantity = amount.getAmount()
                    .divide(lastClosePriceLote.getAmount(), RoundingMode.DOWN)
                    .intValue();

            log.info("QUANTITY FOR TICKER {}: {}, AMOUNT USED: {}", ticker, quantity, amount.getAmount());

            int masterResidualQuantity = masterCustodies.getOrDefault(ticker, 0);

            quantity -= Math.min(quantity, masterResidualQuantity);

            // Calc quantidade por tipo de mercado
            int loteQuantity = Math.floorDiv(quantity, 100);
            int fractionalQuantity = quantity % 100;

            AssetPurchaseDTO response = AssetPurchaseDTO.builder()
                    .ticker(ticker)
                    .quantityToBuy(quantity)
                    .quantityFromMasterAccount(masterResidualQuantity)
                    .marketType(new AssetPurchaseDTO.MarketTypePurchase(loteQuantity, fractionalQuantity))
                    .lastClosePriceLote(lastClosePriceLote)
                    .lastClosePriceFractional(lastClosePriceFractional).build();

            quantityPerAssetResult.put(ticker, response);
        });

        return quantityPerAssetResult;
    }
}
