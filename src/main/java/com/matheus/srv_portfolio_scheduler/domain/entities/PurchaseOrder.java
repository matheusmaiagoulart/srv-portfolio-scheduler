package com.matheus.srv_portfolio_scheduler.domain.entities;

import com.matheus.srv_portfolio_scheduler.domain.enums.MarketType;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.AssetPurchaseDTO;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PurchaseOrder {

    private Long id;
    private long masterAccountId;
    private String ticker;
    private int quantity;
    private Money unitPrice;
    private MarketType marketType;
    private OffsetDateTime executionDate;

    List<Delivery> deliveries;

    public static List<PurchaseOrder> createPurchaseOrders(long masterAccountId, List<AssetPurchaseDTO> assetPurchaseList) {
        if (assetPurchaseList == null || assetPurchaseList.isEmpty()) {
            return Collections.emptyList();
        }

        List<PurchaseOrder> purchaseOrders = new ArrayList<>();

        assetPurchaseList.forEach(asset -> {

            purchaseOrders.add(PurchaseOrder.builder()
                    .masterAccountId(masterAccountId)
                    .ticker(asset.ticker())
                    .quantity(asset.marketType().loteQuantity())
                    .unitPrice(asset.lastClosePriceLote())
                    .marketType(MarketType.BATCH)
                    .executionDate(OffsetDateTime.now())
                    .deliveries(Collections.emptyList())
                    .build());

            if (asset.marketType().fractionalQuantity() > 0) {
                purchaseOrders.add(PurchaseOrder.builder()
                        .masterAccountId(masterAccountId)
                        .ticker(asset.ticker())
                        .quantity(asset.marketType().fractionalQuantity())
                        .unitPrice(asset.lastClosePriceLote())
                        .marketType(MarketType.FRACTIONAL)
                        .executionDate(OffsetDateTime.now())
                        .deliveries(Collections.emptyList())
                        .build());
            }
        });
        return purchaseOrders;
    }

    public static PurchaseOrder reconstruct(Long id, long masterAccountId, String ticker, int quantity, Money unitPrice,
            MarketType marketType, OffsetDateTime executionDate, List<Delivery> deliveries) {
        return PurchaseOrder.builder()
                .id(id)
                .masterAccountId(masterAccountId)
                .ticker(ticker)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .marketType(marketType)
                .executionDate(executionDate)
                .deliveries(deliveries)
                .build();
    }
}
