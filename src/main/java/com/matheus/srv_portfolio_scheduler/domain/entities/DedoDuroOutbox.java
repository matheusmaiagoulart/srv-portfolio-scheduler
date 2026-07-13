package com.matheus.srv_portfolio_scheduler.domain.entities;

import com.matheus.srv_portfolio_scheduler.domain.enums.OutboxStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DedoDuroOutbox {

    private long id;
    String payload;
    OutboxStatus status;
    LocalDateTime createdAt;
    LocalDateTime publishedAt;
    String errorMessage;

    public static DedoDuroOutbox create(String payload) {
        return DedoDuroOutbox.builder()
                .payload(payload)
                .status(OutboxStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .publishedAt(null)
                .errorMessage(null)
                .build();
    }

    public static DedoDuroOutbox reconstruct(
            long id, String payload, OutboxStatus status, LocalDateTime createdAt, LocalDateTime publishedAt, String errorMessage) {
        return DedoDuroOutbox.builder()
                .id(id)
                .payload(payload)
                .status(status)
                .createdAt(createdAt)
                .publishedAt(publishedAt)
                .errorMessage(errorMessage)
                .build();
    }

    public void markAsPublished() {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
        this.errorMessage = null;
    }
}
