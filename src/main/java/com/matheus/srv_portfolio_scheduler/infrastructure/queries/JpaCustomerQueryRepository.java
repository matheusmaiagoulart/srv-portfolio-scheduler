package com.matheus.srv_portfolio_scheduler.infrastructure.queries;

import com.matheus.srv_portfolio_scheduler.infrastructure.entities.JpaCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaCustomerQueryRepository extends JpaRepository<JpaCustomer, Long> {

    @Query("""
    SELECT c FROM customer c
    JOIN FETCH c.brokerageAccount ba
    JOIN FETCH ba.custodies
    WHERE c.id = :customerId
""")
    Optional<JpaCustomer> getCustomerWithCustodiesById(long customerId);
}
