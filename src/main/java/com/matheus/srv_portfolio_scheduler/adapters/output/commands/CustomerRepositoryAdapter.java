package com.matheus.srv_portfolio_scheduler.adapters.output.commands;

import com.matheus.srv_portfolio_scheduler.adapters.mapper.BrokerageAccountMapper;
import com.matheus.srv_portfolio_scheduler.adapters.mapper.CustomerMapper;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.CustomerRepositoryPort;
import com.matheus.srv_portfolio_scheduler.domain.entities.Customer;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.CustodyPurchaseDataDTO;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import com.matheus.srv_portfolio_scheduler.infrastructure.persistence.JpaCustomerRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Component
@AllArgsConstructor
public class CustomerRepositoryAdapter implements CustomerRepositoryPort {

    private final JpaCustomerRepository jpaCustomerRepository;

    @Override
    public void save(Customer customer) {
        jpaCustomerRepository.save(CustomerMapper.toJpaEntity(customer));
    }

    @Override
    public Money getThirdAmountOfAllActiveCustomers() {
        return Money.create(jpaCustomerRepository.getThirdAmountOfAllActiveCustomers());
    }

    @Override
    public Map<Long, CustodyPurchaseDataDTO> getChunkOfCustomers(long lastId, int batchSize) {

        Map<Long, CustodyPurchaseDataDTO> chunkOfCustomers = new HashMap<>();

        Pageable pageable = PageRequest.of(0, batchSize);

        jpaCustomerRepository.getBatchOfCustomers(lastId, pageable)
                .forEach(customer -> {
                    CustodyPurchaseDataDTO dto = new CustodyPurchaseDataDTO(
                            customer.getId(),
                            customer.getName(),
                            customer.getBrokerageAccount().getId(),
                            Money.create(customer.getMonthlyAmount().getAmount().divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_DOWN)),
                            BrokerageAccountMapper.toDomain(customer.getBrokerageAccount(), CustomerMapper.toDomain(customer)));

                    chunkOfCustomers.put(customer.getId(), dto);
                });

        return chunkOfCustomers;
    }
}
