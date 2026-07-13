package com.matheus.srv_portfolio_scheduler.domain.services;

import com.matheus.srv_portfolio_scheduler.domain.entities.BrokerageAccount;
import com.matheus.srv_portfolio_scheduler.domain.entities.Delivery;
import com.matheus.srv_portfolio_scheduler.domain.events.IRDedoDuroEvent;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.CustodyPurchaseDataDTO;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IRDedoDuroCalculator {

    @Value("${app.aliquota}")
    private BigDecimal aliquota;

    public List<IRDedoDuroEvent> calculate(List<Delivery> deliveries, Map<Long, CustodyPurchaseDataDTO> customers) {
        List<BrokerageAccount> customersList = customers.values().stream().map(CustodyPurchaseDataDTO::customerCustodies).toList();
        List<IRDedoDuroEvent> irDedoDuroEvents = new ArrayList<>();

        Map<Long, BrokerageAccount> customerMap = customersList.stream()
                .collect(Collectors.toMap(BrokerageAccount::getId, customer -> customer));

        deliveries.forEach(delivery -> {
            BrokerageAccount customer = customerMap.get(delivery.getCustodyCustomerId());

            BigDecimal operationValue = delivery.getUnitPrice().getAmount()
                    .multiply(BigDecimal.valueOf(delivery.getQuantity()));

            Money irValue = Money.create(operationValue.multiply(aliquota).setScale(2, RoundingMode.HALF_UP));

            irDedoDuroEvents.add(
                    new IRDedoDuroEvent(
                            "IR_DEDO_DURO",
                            customer.getCustomer().getId(),
                            customer.getCustomer().getCpf(),
                            delivery.getTicker(),
                            "COMPRA",
                            delivery.getQuantity(),
                            delivery.getUnitPrice().getAmount(),
                            operationValue,
                            aliquota,
                            irValue.getAmount(),
                            LocalDateTime.now())
            );
        });
        return irDedoDuroEvents;
    }
}
