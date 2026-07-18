package com.matheus.srv_portfolio_scheduler.adapters.output.commands;

import com.matheus.srv_portfolio_scheduler.adapters.mapper.DeliveryMapper;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.DeliveryRepositoryPort;
import com.matheus.srv_portfolio_scheduler.domain.entities.Delivery;
import com.matheus.srv_portfolio_scheduler.infrastructure.entities.JpaDelivery;
import com.matheus.srv_portfolio_scheduler.infrastructure.persistence.JpaDeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DeliveryRepositoryAdapter implements DeliveryRepositoryPort {

    private final JpaDeliveryRepository repository;

    @Override
    public void save(Delivery delivery) {
        repository.save(DeliveryMapper.toJpaEntity(delivery));
    }

    @Override
    public void saveAll(List<Delivery> deliveries) {
        List<JpaDelivery> jpaDeliveries = deliveries.stream()
                .map(DeliveryMapper::toJpaEntity)
                .toList();
        repository.saveAll(jpaDeliveries);
    }
}
