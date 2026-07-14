package com.matheus.srv_portfolio_scheduler.domain.services.dto;

import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import lombok.Builder;

@Builder
public record AssetPurchaseDTO(
        String ticker,
        Money lastClosePriceLote,
        Money lastClosePriceFractional,
        int quantityToBuy,
        int quantityFromMasterAccount,
        int totalQuantity,
        MarketTypePurchase marketType
) {
    public record MarketTypePurchase(int loteQuantity, int fractionalQuantity) {
    }

    @Override
    public int totalQuantity() {
        return quantityToBuy + quantityFromMasterAccount;
    }
}
