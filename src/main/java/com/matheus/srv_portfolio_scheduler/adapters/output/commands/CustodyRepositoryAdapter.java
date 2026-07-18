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

        return saveResult.stream()
                .map(jpaCustody -> {
                    var domainCustomer = CustomerMapper.toDomain(jpaCustody.getBrokerageAccount().getCustomer());
                    var domainBrokerage = BrokerageAccountMapper.toDomain(jpaCustody.getBrokerageAccount(), domainCustomer);
                    return CustodyMapper.toDomain(jpaCustody, domainBrokerage);
                }).toList();
    }
}
