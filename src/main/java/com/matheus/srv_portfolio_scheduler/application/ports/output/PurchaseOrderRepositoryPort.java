package com.matheus.srv_portfolio_scheduler.application.ports.output;

import com.matheus.srv_portfolio_scheduler.domain.entities.PurchaseOrder;

import java.util.List;

public interface PurchaseOrderRepositoryPort {

    PurchaseOrder save(PurchaseOrder purchaseOrder);

    List<PurchaseOrder> save(List<PurchaseOrder> purchaseOrders);
}
