package com.matheus.srv_portfolio_scheduler.adapters.output;

import com.matheus.srv_portfolio_scheduler.adapters.mapper.DedoDuroOutboxMapper;
import com.matheus.srv_portfolio_scheduler.application.ports.output.DedoDuroOutboxRepositoryPort;
import com.matheus.srv_portfolio_scheduler.domain.entities.DedoDuroOutbox;
import com.matheus.srv_portfolio_scheduler.infrastructure.persistence.JpaDedoDuroOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DedoDuroOutboxAdapter implements DedoDuroOutboxRepositoryPort {

    private final JpaDedoDuroOutboxRepository repository;

    @Override
    public void saveAll(List<DedoDuroOutbox> dedoDuroOutboxList) {
        repository.saveAll(dedoDuroOutboxList.stream().map(DedoDuroOutboxMapper::toJpaEntity).toList());
    }

    @Override
    public void update(DedoDuroOutbox dedoDuroOutbox) {
        repository.save(DedoDuroOutboxMapper.toJpaEntity(dedoDuroOutbox));
    }

    @Override
    public List<DedoDuroOutbox> getChunckOfOutboxes() {
        return List.of();
    }
}
