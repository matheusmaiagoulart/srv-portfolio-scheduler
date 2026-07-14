package com.matheus.srv_portfolio_scheduler.infrastructure.persistence;

import com.matheus.srv_portfolio_scheduler.infrastructure.entities.JpaCustomer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface JpaCustomerRepository extends JpaRepository<JpaCustomer, Long> {


    @Query(""" 
                SELECT c FROM customer c
                JOIN FETCH c.brokerageAccount ba
                JOIN FETCH ba.custodies
                WHERE ba.accountType = 'MASTER'
            """)
    Optional<JpaCustomer> getMasterAccount();

    @Query("""
                SELECT SUM(c.monthlyAmount.amount / 3) FROM customer c
                JOIN c.brokerageAccount ba
                WHERE ba.accountType != 'MASTER' AND c.active = true
            """)
    BigDecimal getThirdAmountOfAllActiveCustomers();

    @Query("""
                  SELECT c FROM customer c
                  JOIN FETCH c.brokerageAccount ba
                  JOIN FETCH ba.custodies
                  WHERE c.active = true
                    AND c.brokerageAccount.accountType != 'MASTER'
                    AND c.id > :lastId
                  ORDER BY c.id ASC
            """)
    List<JpaCustomer> getBatchOfCustomers(long lastId, Pageable pageable);
}
