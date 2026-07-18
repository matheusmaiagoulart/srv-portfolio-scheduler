package com.matheus.srv_portfolio_scheduler.application.service;

import com.matheus.srv_portfolio_scheduler.application.ports.output.DedoDuroOutboxRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.ports.output.messaging.KafkaProducerPort;
import com.matheus.srv_portfolio_scheduler.domain.entities.DedoDuroOutbox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IRDedoDuroOutboxService {
    
    private final ObjectMapper objectMapper;
    private final KafkaProducerPort kafkaProducer;
    private final DedoDuroOutboxRepositoryPort dedoDuroOutboxRepository;

    public List<DedoDuroOutbox> createOutboxEntries(List<String> payloads) {
        return payloads.stream()
                .map(DedoDuroOutbox::create)
                .toList();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendOutboxToBroker(DedoDuroOutbox outbox, String topic) {
        log.info("Sending outbox entry with ID {} to topic {}", outbox.getId(), topic);

        try {
            var payload = objectMapper.writeValueAsString(outbox.getPayload());
            kafkaProducer.send(payload, topic);
            outbox.markAsPublished();
            dedoDuroOutboxRepository.update(outbox);
        } catch (Exception e) {
            log.error("Failed to send message to broker. Outbox ID={} ERROR={}", outbox.getId(), e.getMessage());
            outbox.markAsFailed(e.getMessage());
            dedoDuroOutboxRepository.update(outbox);
        }
    }
}
