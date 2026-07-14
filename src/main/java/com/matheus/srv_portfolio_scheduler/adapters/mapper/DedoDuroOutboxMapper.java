package com.matheus.srv_portfolio_scheduler.adapters.mapper;

import com.matheus.srv_portfolio_scheduler.domain.entities.DedoDuroOutbox;
import com.matheus.srv_portfolio_scheduler.infrastructure.entities.JpaDedoDuroOutbox;

public class DedoDuroOutboxMapper {

    public static JpaDedoDuroOutbox toJpaEntity(DedoDuroOutbox outbox) {
        return JpaDedoDuroOutbox.builder()
                .id(outbox.getId())
                .payload(outbox.getPayload())
                .status(outbox.getStatus())
                .attempts(outbox.getAttempts())
                .createdAt(outbox.getCreatedAt())
                .publishedAt(outbox.getPublishedAt())
                .errorMessage(outbox.getErrorMessage())
                .build();
    }

    public static DedoDuroOutbox toDomain(JpaDedoDuroOutbox outbox) {
        return DedoDuroOutbox.reconstruct(
                outbox.getId(),
                outbox.getPayload(),
                outbox.getStatus(),
                outbox.getAttempts(),
                outbox.getCreatedAt(),
                outbox.getPublishedAt(),
                outbox.getErrorMessage()
        );
    }
}
