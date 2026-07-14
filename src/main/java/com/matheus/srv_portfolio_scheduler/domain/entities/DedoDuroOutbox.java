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
    private String payload;
    private OutboxStatus status;
    private int attempts;
    private  LocalDateTime createdAt;
    private LocalDateTime publishedAt;
    private String errorMessage;

    public static DedoDuroOutbox create(String payload) {
        return DedoDuroOutbox.builder()
                .payload(payload)
                .status(OutboxStatus.PENDING)
                .attempts(0)
                .createdAt(LocalDateTime.now())
                .publishedAt(null)
                .errorMessage(null)
                .build();
    }

    public static DedoDuroOutbox reconstruct(
            long id, String payload, OutboxStatus status, int attempts, LocalDateTime createdAt, LocalDateTime publishedAt, String errorMessage) {
        return DedoDuroOutbox.builder()
                .id(id)
                .payload(payload)
                .status(status)
                .attempts(attempts)
                .createdAt(createdAt)
                .publishedAt(publishedAt)
                .errorMessage(errorMessage)
                .build();
    }

    public void markAsPublished() {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    public void markAsFailed(String errorMessage) {
        this.errorMessage = errorMessage;
        this.attempts = this.attempts + 1;
    }
}
