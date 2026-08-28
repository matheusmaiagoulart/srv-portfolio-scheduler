package com.matheus.srv_portfolio_scheduler.application.service;

import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.DedoDuroOutboxRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.ports.output.messaging.KafkaProducerPort;
import com.matheus.srv_portfolio_scheduler.domain.entities.DedoDuroOutbox;
import com.matheus.srv_portfolio_scheduler.domain.enums.OutboxStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IRDedoDuroOutboxServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private KafkaProducerPort kafkaProducer;

    @Mock
    private DedoDuroOutboxRepositoryPort dedoDuroOutboxRepository;

    @InjectMocks
    private IRDedoDuroOutboxService service;

    @Test
    void deveCriarEntradasDeOutboxParaCadaPayload() {
        List<String> payloads = List.of(
                "{\"ticker\":\"PETR4\",\"value\":280.00}",
                "{\"ticker\":\"VALE3\",\"value\":248.00}");

        List<DedoDuroOutbox> result = service.createOutboxEntries(payloads);

        assertEquals(2, result.size());
        assertNotNull(result.get(0).getPayload());
        assertNotNull(result.get(1).getPayload());
    }

    @Test
    void deveRetornarListaVaziaQuandoPayloadsVazio() {
        List<String> payloads = List.of();

        List<DedoDuroOutbox> result = service.createOutboxEntries(payloads);

        assertTrue(result.isEmpty());
    }

    @Test
    void deveEnviarOutboxParaBrokerComSucesso() throws Exception {
        DedoDuroOutbox outbox = DedoDuroOutbox.create("{\"ticker\":\"PETR4\"}");
        String topic = "ir-dedo-duro-topic";

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"ticker\":\"PETR4\"}");
        doNothing().when(kafkaProducer).send(any(), eq(topic));

        service.sendOutboxToBroker(outbox, topic);

        verify(kafkaProducer).send(any(), eq(topic));
        verify(dedoDuroOutboxRepository).update(outbox);
        assertEquals(OutboxStatus.PUBLISHED, outbox.getStatus());
    }

    @Test
    void deveMarcarStatusQuandoEnvioFalhar() throws Exception {
        DedoDuroOutbox outbox = DedoDuroOutbox.create("{\"ticker\":\"PETR4\"}");
        String topic = "ir-dedo-duro-topic";

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"ticker\":\"PETR4\"}");
        doThrow(new RuntimeException("Kafka unavailable")).when(kafkaProducer).send(any(), eq(topic));

        service.sendOutboxToBroker(outbox, topic);

        verify(dedoDuroOutboxRepository).update(outbox);
        assertNotEquals(OutboxStatus.PUBLISHED, outbox.getStatus());
    }
}
