package com.matheus.srv_portfolio_scheduler.infrastructure.entities;

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
    String payload;
    @Enumerated(EnumType.STRING)
    OutboxStatus status;
    LocalDateTime createdAt;
    LocalDateTime publishedAt;
    String errorMessage;
}
