package com.matheus.srv_portfolio_scheduler.adapters.mapper;

import com.matheus.srv_portfolio_scheduler.domain.entities.PurchaseOrder;
import com.matheus.srv_portfolio_scheduler.infrastructure.entities.JpaPurchaseOrder;

public class PurchaseOrderMapper {

    public static JpaPurchaseOrder toJpaEntity(PurchaseOrder purchaseOrder) {
        return JpaPurchaseOrder.builder()
                .id(purchaseOrder.getId())
                .masterAccountId(purchaseOrder.getMasterAccountId())
                .ticker(purchaseOrder.getTicker())
                .quantity(purchaseOrder.getQuantity())
                .unitPrice(purchaseOrder.getUnitPrice())
                .marketType(purchaseOrder.getMarketType())
                .executionDate(purchaseOrder.getExecutionDate())
                .build();
    }

    public static PurchaseOrder toDomain(JpaPurchaseOrder jpaPurchaseOrder) {
        return PurchaseOrder.reconstruct(
                jpaPurchaseOrder.getId(),
                jpaPurchaseOrder.getMasterAccountId(),
                jpaPurchaseOrder.getTicker(),
                jpaPurchaseOrder.getQuantity(),
                jpaPurchaseOrder.getUnitPrice(),
                jpaPurchaseOrder.getMarketType(),
                jpaPurchaseOrder.getExecutionDate(),
                jpaPurchaseOrder.getDeliveries().stream().map(DeliveryMapper::toDomain).toList()
        );
    }
}
