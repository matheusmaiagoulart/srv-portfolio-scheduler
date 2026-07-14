package com.matheus.srv_portfolio_scheduler.adapters.input.scheduler;

import com.matheus.srv_portfolio_scheduler.application.ports.output.DedoDuroOutboxRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.service.IRDedoDuroOutboxService;
import com.matheus.srv_portfolio_scheduler.domain.entities.DedoDuroOutbox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IRDedoDuroScheduler {

    private final DedoDuroOutboxRepositoryPort outboxRepositoryPort;
    private final IRDedoDuroOutboxService outboxService;

    @Value("${spring.kafka.topics.ir-dedo-duro-compra-topic}")
    private String TOPIC;

    @Value("${app.outbox_batch_size}")
    private int OUTBOX_BATCH_SIZE;

    @Scheduled(cron = "0 0 22 5 * ?")
    @Scheduled(cron = "0 0 22 15 * ?")
    @Scheduled(cron = "0 0 22 25 * ?")
    @Scheduled(fixedDelay = 3000)
    public void processPendingOutboxes() {
        // get chunk of outbox
        log.info("STARTING PENDING OUTBOX SCHEDULER");
        List<DedoDuroOutbox> outboxList = outboxRepositoryPort.getChunkOfOutboxes(OUTBOX_BATCH_SIZE);

        if (outboxList.isEmpty()) return;

        log.info("STARTING SENDING OUTBOX SCHEDULER");
        outboxList.forEach(outbox -> {
            outboxService.sendOutboxToBroker(outbox, TOPIC);
        });

        log.info("ENDING PENDING OUTBOX SCHEDULER");
    }
}
