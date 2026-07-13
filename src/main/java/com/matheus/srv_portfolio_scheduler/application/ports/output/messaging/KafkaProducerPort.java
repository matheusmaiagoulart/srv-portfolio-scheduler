package com.matheus.srv_portfolio_scheduler.application.ports.output.messaging;

public interface KafkaProducerPort {
    void send(String message, String topic);
}
