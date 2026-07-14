package com.matheus.srv_portfolio_scheduler.infrastructure.persistence;

import com.matheus.srv_portfolio_scheduler.infrastructure.entities.JpaBrokerageAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaBrokerageAccountRepository extends JpaRepository<JpaBrokerageAccount, Long> {

    @Query("""
            SELECT ba FROM brokerage_account ba
                        JOIN FETCH ba.customer c
                        JOIN FETCH ba.custodies
                WHERE ba.accountType = 'MASTER'
            """)
    Optional<JpaBrokerageAccount> getMasterAccount();
}
