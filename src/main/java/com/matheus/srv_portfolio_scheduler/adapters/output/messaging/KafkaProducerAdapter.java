package com.matheus.srv_portfolio_scheduler.adapters.output.messaging;

import com.matheus.srv_portfolio_scheduler.application.ports.output.messaging.KafkaProducerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class KafkaProducerAdapter implements KafkaProducerPort {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public void send(String payload, String topic) {

            Message<String> message = MessageBuilder
                    .withPayload(payload)
                    .setHeader(KafkaHeaders.TOPIC, topic)
                    .build();

            CompletableFuture<SendResult<String, String>> result = kafkaTemplate.send(message);

            result.whenComplete((sendResult, throwable) -> {
                if (throwable != null) {
                    throw new RuntimeException("Kafka send failed for reason: ", throwable.getCause());
                }
            });
        }
}
