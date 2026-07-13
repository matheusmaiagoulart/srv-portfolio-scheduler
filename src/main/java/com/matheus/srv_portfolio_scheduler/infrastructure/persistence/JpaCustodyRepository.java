package com.matheus.srv_portfolio_scheduler.infrastructure.persistence;

import com.matheus.srv_portfolio_scheduler.infrastructure.entities.JpaCustody;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaCustodyRepository extends JpaRepository<JpaCustody, Long> {
}
