package com.matheus.srv_portfolio_scheduler.adapters.output.commands;

import com.matheus.srv_portfolio_scheduler.adapters.mapper.BrokerageAccountMapper;
import com.matheus.srv_portfolio_scheduler.adapters.mapper.CustodyMapper;
import com.matheus.srv_portfolio_scheduler.adapters.mapper.CustomerMapper;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.CustodyRepositoryPort;
import com.matheus.srv_portfolio_scheduler.domain.entities.Custody;
import com.matheus.srv_portfolio_scheduler.infrastructure.entities.JpaCustody;
import com.matheus.srv_portfolio_scheduler.infrastructure.persistence.JpaCustodyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CustodyRepositoryAdapter implements CustodyRepositoryPort {

    private final JpaCustodyRepository customerRepository;

    @Override
    public List<Custody> saveAll(List<Custody> custodies) {

        List<JpaCustody> jpaCustodies = custodies.stream()
                .map(custody -> {
                    var jpaCustomer = CustomerMapper.toJpaEntity(custody.getBrokerageAccount().getCustomer());
                    var jpaBrokerage = BrokerageAccountMapper.toJpaEntity(custody.getBrokerageAccount(), jpaCustomer);
                    return CustodyMapper.toJpaEntity(custody, jpaBrokerage);
                }).toList();

        List<JpaCustody> saveResult = customerRepository.saveAll(jpaCustodies);

        List<Custody> persistedCustodies = new ArrayList<>(saveResult.size());
        for (int index = 0; index < saveResult.size(); index++) {
            persistedCustodies.add(CustodyMapper.toDomain(
                    saveResult.get(index),
                    custodies.get(index).getBrokerageAccount()));
        }

        return persistedCustodies;
    }
}
