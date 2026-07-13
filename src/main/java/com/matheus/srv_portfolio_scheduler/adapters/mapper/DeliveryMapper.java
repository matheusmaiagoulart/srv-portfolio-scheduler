package com.matheus.srv_portfolio_scheduler.adapters.mapper;

import com.matheus.srv_portfolio_scheduler.domain.entities.Delivery;
import com.matheus.srv_portfolio_scheduler.infrastructure.entities.JpaDelivery;
import com.matheus.srv_portfolio_scheduler.infrastructure.entities.JpaPurchaseOrder;

public class DeliveryMapper {

    public static JpaDelivery toJpaEntity(Delivery delivery) {
        JpaPurchaseOrder purchaseOrderRef = JpaPurchaseOrder.builder()
                .id(delivery.getPurchaseOrderId())
                .build();
        return toJpaEntity(delivery, purchaseOrderRef);
    }

    public static JpaDelivery toJpaEntity(Delivery delivery, JpaPurchaseOrder purchaseOrder) {
        return JpaDelivery.builder()
                .id(delivery.getId())
                .purchaseOrder(purchaseOrder)
                .custodyCustomerId(delivery.getCustodyCustomerId())
                .ticker(delivery.getTicker())
                .quantity(delivery.getQuantity())
                .unitPrice(delivery.getUnitPrice())
                .deliveryDate(delivery.getDeliveryDate())
                .build();
    }

    public static Delivery toDomain(JpaDelivery delivery) {
        return Delivery.reconstruct(
                delivery.getId(),
                delivery.getPurchaseOrder().getId(),
                delivery.getCustodyCustomerId(),
                delivery.getTicker(),
                delivery.getQuantity(),
                delivery.getUnitPrice(),
                delivery.getDeliveryDate()
        );
    }
}
