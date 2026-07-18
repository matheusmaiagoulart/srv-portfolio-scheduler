package com.matheus.srv_portfolio_scheduler.adapters.output.commands;

import com.matheus.srv_portfolio_scheduler.adapters.mapper.PurchaseOrderMapper;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.PurchaseOrderRepositoryPort;
import com.matheus.srv_portfolio_scheduler.domain.entities.PurchaseOrder;
import com.matheus.srv_portfolio_scheduler.infrastructure.entities.JpaPurchaseOrder;
import com.matheus.srv_portfolio_scheduler.infrastructure.persistence.JpaPurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PurchaseOrderRepositoryAdapter implements PurchaseOrderRepositoryPort {

    private final JpaPurchaseOrderRepository jpaPurchaseOrder;

    @Override
    public PurchaseOrder save(PurchaseOrder purchaseOrder) {
        return PurchaseOrderMapper.toDomain(jpaPurchaseOrder.saveAndFlush(PurchaseOrderMapper.toJpaEntity(purchaseOrder)));
    }

    @Override
    public List<PurchaseOrder> save(List<PurchaseOrder> purchaseOrders) {
        List<JpaPurchaseOrder> orders = purchaseOrders.stream().map(PurchaseOrderMapper::toJpaEntity).toList();
        List<JpaPurchaseOrder> ordersSaved = jpaPurchaseOrder.saveAllAndFlush(orders);

        return ordersSaved.stream().map(PurchaseOrderMapper::toDomain).toList();
    }
}
