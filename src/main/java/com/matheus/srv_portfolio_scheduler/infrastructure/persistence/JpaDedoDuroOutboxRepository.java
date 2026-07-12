package com.matheus.srv_portfolio_scheduler.infrastructure.persistence;

import com.matheus.srv_portfolio_scheduler.domain.entities.DedoDuroOutbox;
import com.matheus.srv_portfolio_scheduler.infrastructure.entities.JpaDedoDuroOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface JpaDedoDuroOutboxRepository extends JpaRepository<JpaDedoDuroOutbox, Long> {

    @Query("""
        SELECT o FROM dedo_duro_outbox o
        WHERE o.status = 'PENDING'
        ORDER BY o.createdAt
        LIMIT :batch_size
""")
    List<DedoDuroOutbox> getChunkOfOutboxes(int batch_size);
}
