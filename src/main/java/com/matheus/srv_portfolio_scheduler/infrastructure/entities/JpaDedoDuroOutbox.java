package com.matheus.srv_portfolio_scheduler.infrastructure.entities;

import com.fasterxml.jackson.annotation.JsonValue;
import com.matheus.srv_portfolio_scheduler.domain.enums.OutboxStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@Table(name = "ir_dedo_duro_outbox")
@Entity(name = "dedo_duro_outbox")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JpaDedoDuroOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String payload;

    @Enumerated(EnumType.STRING)
    private OutboxStatus status;

    private int attempts;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
    private String errorMessage;
}
