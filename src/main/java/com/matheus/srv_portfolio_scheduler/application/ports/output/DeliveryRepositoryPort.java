package com.matheus.srv_portfolio_scheduler.application.ports.output;

import com.matheus.srv_portfolio_scheduler.domain.entities.Delivery;

import java.util.List;

public interface DeliveryRepositoryPort {
    void save(Delivery delivery);
    void saveAll (List<Delivery> deliveries);
}
