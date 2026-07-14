package com.matheus.srv_portfolio_scheduler.application.ports.output;

import com.matheus.srv_portfolio_scheduler.domain.entities.DedoDuroOutbox;

import java.util.List;

public interface DedoDuroOutboxRepositoryPort {
    void saveAll(List<DedoDuroOutbox> dedoDuroOutboxes);
    void update(DedoDuroOutbox dedoDuroOutbox);
    List<DedoDuroOutbox> getChunkOfOutboxes(int batch_size);
}
