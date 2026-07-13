package com.matheus.srv_portfolio_scheduler.adapters.mapper;

import com.matheus.srv_portfolio_scheduler.domain.entities.BrokerageAccount;
import com.matheus.srv_portfolio_scheduler.domain.entities.Custody;
import com.matheus.srv_portfolio_scheduler.infrastructure.entities.JpaBrokerageAccount;
import com.matheus.srv_portfolio_scheduler.infrastructure.entities.JpaCustody;

public class CustodyMapper {

    public static JpaCustody toJpaEntity(Custody custody, JpaBrokerageAccount brokerageAccount) {
        return JpaCustody.builder()
                .id(custody.getId())
                .brokerageAccount(brokerageAccount)
                .ticker(custody.getTicker())
                .quantity(custody.getQuantity())
                .averagePrice(custody.getAveragePrice())
                .lastUpdate(custody.getLastUpdate())
                .build();
    }

    public static Custody toDomain(JpaCustody custody, BrokerageAccount brokerageAccount) {
        return Custody.reconstruct(
                custody.getId(),
                brokerageAccount,
                custody.getTicker(),
                custody.getQuantity(),
                custody.getAveragePrice(),
                custody.getLastUpdate()
        );
    }
}
