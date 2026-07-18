package com.matheus.srv_portfolio_scheduler.adapters.input.scheduler;

import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.DedoDuroOutboxRepositoryPort;
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

    private final IRDedoDuroOutboxService outboxService;
    private final DedoDuroOutboxRepositoryPort outboxRepositoryPort;

    @Value("${spring.kafka.topics.ir-dedo-duro-compra-topic}")
    private String TOPIC;

    @Value("${app.outbox_batch_size}")
    private int OUTBOX_BATCH_SIZE;

    @Scheduled(cron = "0 0 22 5 * ?")
    @Scheduled(cron = "0 0 22 15 * ?")
    @Scheduled(cron = "0 0 22 25 * ?")
    //@Scheduled(fixedDelay = 3000)
    public void processPendingOutboxes() {
        log.info("OUTBOX SCHEDULER STARTED");

        long init = System.currentTimeMillis();
        long durationTime;

        List<DedoDuroOutbox> outboxList;
        int totalProcessed = 0;

        do {
            outboxList = outboxRepositoryPort.getChunkOfOutboxes(OUTBOX_BATCH_SIZE);

            if (outboxList.isEmpty()) break;

            log.info("STARTING SENDING OUTBOX SCHEDULER");
            outboxList.forEach(outbox -> {
                outboxService.sendOutboxToBroker(outbox, TOPIC);
            });

            totalProcessed += outboxList.size();
        } while (outboxList.size() == OUTBOX_BATCH_SIZE);

        durationTime = System.currentTimeMillis() - init;

        log.info("SCHEDULER FINISHED | DurationTime={} | TotalProcessed={}",
                transformDurationTime(durationTime), totalProcessed);

    }

    private String transformDurationTime(long durationTime) {
        if (durationTime > 1000) {
            var seconds = durationTime / 1000;
            var ms = durationTime % 1000;
            return String.format("%dsec %dms", seconds, ms);
        }

        return String.format("%dms", durationTime);
    }
}
