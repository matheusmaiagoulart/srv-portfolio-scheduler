package com.matheus.srv_portfolio_scheduler.domain.entities;

import com.matheus.srv_portfolio_scheduler.domain.enums.BrokerageAccountType;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Custody {

    private Long id;

    private BrokerageAccount brokerageAccount;

    private String ticker;
    private int quantity;
    private Money averagePrice;
    private OffsetDateTime lastUpdate;

    public static Custody create(BrokerageAccount brokerageAccount, String ticker) {
        return Custody.builder()
                .brokerageAccount(brokerageAccount)
                .ticker(ticker)
                .quantity(0)
                .averagePrice(Money.create(BigDecimal.ZERO))
                .lastUpdate(OffsetDateTime.now())
                .build();
    }

    public static Custody reconstruct(Long id, BrokerageAccount brokerageAccount, String ticker, int quantity, Money averagePrice, OffsetDateTime lastUpdate) {
        return Custody.builder()
                .id(id)
                .brokerageAccount(brokerageAccount)
                .ticker(ticker)
                .quantity(quantity)
                .averagePrice(averagePrice)
                .lastUpdate(lastUpdate)
                .build();
    }

    public void addPurchaseQuantity(int quantity, Money price) {
        updateAveragePrice(quantity, price);
        this.quantity += quantity;
        this.lastUpdate = OffsetDateTime.now();
    }

    public void updateAveragePrice(int newQuantity, Money newPrice) {
        int totalQuantity = this.quantity + newQuantity;
        if (totalQuantity == 0) this.averagePrice = Money.create(BigDecimal.ZERO);

        // (averagePrice * quantity) + (newPrice * newQuantity) / totalQuantity
        BigDecimal newAveragePrice = (averagePrice.getAmount().multiply(BigDecimal.valueOf(quantity)))
                .add(newPrice.getAmount().multiply(BigDecimal.valueOf(newQuantity)))
                .divide(BigDecimal.valueOf(totalQuantity), 2, RoundingMode.HALF_UP);

        this.averagePrice = Money.create(newAveragePrice);
    }

    public void updateResidualQuantity(int residualQuantity, Money price, BrokerageAccountType accountType) {
        if (accountType == BrokerageAccountType.MASTER) {
            this.quantity = residualQuantity;
            updateAveragePrice(residualQuantity, price);
            this.lastUpdate = OffsetDateTime.now();
        }
    }

    /**
     * Calculates the Profit and Loss (P/L) of this custody position.
     * <p>
     * Formula: (currentPrice - averagePrice) × quantity
     * <p>
     * A positive value indicates profit; a negative value indicates loss.
     * Example: bought 68 PETR4 at R$49.41, current price R$55.00 → P/L = R$380.12
     *
     * @param currentPrice the current market price of the asset
     * @return the absolute P/L value in BRL
     */
    public Money calcPl(Money currentPrice) {
        return Money.create(currentPrice.getAmount().subtract(this.averagePrice.getAmount())
                .multiply(BigDecimal.valueOf(this.quantity)));
    }

    /**
     * Calculates the Profit and Loss percentage (P/L%) of this custody position.
     * <p>
     * Formula: ((currentPrice - averagePrice) / averagePrice) × 100
     * <p>
     * Represents how much the asset has appreciated or depreciated relative to the purchase price.
     * Example: bought at R$49.41, current price R$55.00 → P/L% = 11.31%
     *
     * @param currentPrice the current market price of the asset
     * @return the P/L percentage with 2 decimal places
     */
    public BigDecimal calcPlPercentual(Money currentPrice) {
        if (this.averagePrice.getAmount().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return currentPrice.getAmount().subtract(this.averagePrice.getAmount())
                .divide(this.averagePrice.getAmount(), 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates the composition percentage of this asset within the total portfolio.
     * <p>
     * Formula: (currentPrice × quantity) / portfolioCurrentValue × 100
     * <p>
     * Represents how much weight this asset holds in the overall portfolio at current market prices.
     * Example: PETR4 current value = R$3,740 out of R$11,433.25 total → composition = 32.71%
     *
     * @param currentPrice          the current market price of the asset
     * @param portfolioCurrentValue the total current market value of all custodies combined
     * @return the composition percentage with 4 decimal places precision
     */
    public Money calcCompositionPercentage(Money currentPrice, Money portfolioCurrentValue) {
        if (portfolioCurrentValue.getAmount().compareTo(BigDecimal.ZERO) == 0) {
            return Money.create(BigDecimal.ZERO);
        }

        BigDecimal currentValue = currentPrice.getAmount().multiply(BigDecimal.valueOf(this.quantity));

        return Money.create(
                currentValue.divide(portfolioCurrentValue.getAmount(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)));
    }
}
