package com.matheus.srv_portfolio_scheduler.infrastructure.persistence;

import com.matheus.srv_portfolio_scheduler.infrastructure.entities.JpaDedoDuroOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaDedoDuroOutboxRepository extends JpaRepository<JpaDedoDuroOutbox, Long> {

    @Query(value = """
        SELECT TOP(:batch_size) * FROM ir_dedo_duro_outbox o
        WHERE o.status = 'PENDING' AND o.attempts < 3
        ORDER BY o.created_at ASC
""", nativeQuery = true)
    List<JpaDedoDuroOutbox> getChunkOfOutboxes(@Param("batch_size") int batch_size);
}
