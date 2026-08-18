package com.matheus.srv_portfolio_scheduler.application.service;

import com.matheus.srv_portfolio_scheduler.domain.services.dto.PortfolioComparisonDTO;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.BrokerageAccountRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.CustomerRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.PortfolioRebalanceRepositoryPort;
import com.matheus.srv_portfolio_scheduler.domain.entities.BrokerageAccount;
import com.matheus.srv_portfolio_scheduler.domain.entities.Custody;
import com.matheus.srv_portfolio_scheduler.domain.entities.PortfolioRebalance;
import com.matheus.srv_portfolio_scheduler.domain.enums.RebalanceType;
import com.matheus.srv_portfolio_scheduler.domain.services.PortfolioAnalytics;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.CustodyPurchaseDataDTO;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.CustomerBuyNeedDTO;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.RebalanceExecutionResultDTO;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.RebalanceResultDTO;
import com.matheus.srv_portfolio_scheduler.domain.services.rebalance.CalculateBuyNeedOperation;
import com.matheus.srv_portfolio_scheduler.domain.services.rebalance.CustodyTickerMigrationOperation;
import com.matheus.srv_portfolio_scheduler.domain.services.rebalance.SellExcessOperation;
import com.matheus.srv_portfolio_scheduler.domain.services.rebalance.SellRemovedAssetsOperation;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioRebalanceService {

    private final SellExcessOperation sellExcessOperation;
    private final CustomerRepositoryPort customerRepository;
    private final BrokerageAccountRepositoryPort brokerageRepository;
    private final CalculateBuyNeedOperation calculateBuyNeedOperation;
    private final SellRemovedAssetsOperation sellRemovedAssetsOperation;
    private final PortfolioRebalanceRepositoryPort portfolioRebalanceRepository;
    private final CustodyTickerMigrationOperation custodyTickerMigrationOperation;


    @Value("${app.customers_batch_size}")
    private int batch_size;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RebalanceExecutionResultDTO execute(
            BrokerageAccount masterAccount,
            PortfolioComparisonDTO comparisonDTO,
            Map<String, Money> pricesByTicker) {

        long lastId = 0;
        Money totaReleasedAmount = Money.create(BigDecimal.ZERO);
        List<CustomerBuyNeedDTO> customerBuyNeeds = new ArrayList<>();

        while (true) {
            Map<Long, CustodyPurchaseDataDTO> customersChunk = customerRepository.getChunkOfCustomers(lastId, batch_size);

            if (customersChunk == null || customersChunk.isEmpty()) break;

            for (CustodyPurchaseDataDTO customerData : customersChunk.values()) {
                CustomerRebalanceOutcome outcome = processCustomerRebalance(customerData, comparisonDTO, pricesByTicker);
                totaReleasedAmount = totaReleasedAmount.add(outcome.releasedAmount());
                customerBuyNeeds.add(outcome.buyNeed());
            }
            lastId = customersChunk.keySet().stream().max(Long::compareTo).orElse(lastId);

            if (customersChunk.size() < batch_size) {
                break;
            }
        }

        log.info("Rebalance executed successfully");
            // Update Master removed assets and sold positions
            RebalanceResultDTO masterResult = soldRemovedPositionsFromMaster(
                    masterAccount,
                    comparisonDTO.removed().stream()
                            .map(PortfolioComparisonDTO.RemovedItem::ticker)
                            .collect(Collectors.toList())
            );
            totaReleasedAmount = totaReleasedAmount.add(masterResult.totalReleased());

            brokerageRepository.save(masterAccount);

            return new RebalanceExecutionResultDTO(totaReleasedAmount, customerBuyNeeds);
    }

    private CustomerRebalanceOutcome processCustomerRebalance(
            CustodyPurchaseDataDTO customerData,
            PortfolioComparisonDTO comparisonDTO,
            Map<String, Money> pricesByTicker) {

        Long customerId = customerData.customerId();
        Money releasedAmount = Money.create(BigDecimal.ZERO);

        Map<String, Custody> custodies = customerData.getCustomerCustodies()
                .getCustodies().stream()
                .collect(Collectors.toMap(Custody::getTicker, c -> c));

        Money originalPortfolioValue = PortfolioAnalytics.calcPortfolioTotalValue(
                customerData.getCustomerCustodies().getCustodies(), pricesByTicker);

        // Vende removed
        RebalanceResultDTO removedResult = sellRemovedAssetsOperation.execute(
                custodies,
                comparisonDTO.removed().stream()
                        .map(PortfolioComparisonDTO.RemovedItem::ticker)
                        .collect(Collectors.toSet()),
                pricesByTicker,
                customerId
        );
        releasedAmount = releasedAmount.add(removedResult.totalReleased());

        // Vende excess
        RebalanceResultDTO excessResult = sellExcessOperation.execute(
                custodies,
                comparisonDTO.altered().stream()
                        .collect(Collectors.toMap(
                                PortfolioComparisonDTO.AlteredItem::ticker,
                                PortfolioComparisonDTO.AlteredItem::newPercentage
                        )),
                pricesByTicker,
                customerId,
                originalPortfolioValue
        );
        releasedAmount = releasedAmount.add(excessResult.totalReleased());

        // Migra tickers
        custodyTickerMigrationOperation.execute(
                custodies,
                comparisonDTO.removed().stream()
                        .map(PortfolioComparisonDTO.RemovedItem::ticker)
                        .collect(Collectors.toSet()),
                comparisonDTO.added().stream()
                        .map(PortfolioComparisonDTO.NewItem::ticker)
                        .collect(Collectors.toSet())
        );

        // Calcula quanto o cliente precisa comprar de cada ticker (novos + aumentados)
        // apos as vendas/migracao ja terem sido aplicadas nas custodias em memoria
        CustomerBuyNeedDTO buyNeed = calculateBuyNeedOperation.execute(
                customerId,
                custodies,
                comparisonDTO,
                pricesByTicker,
                originalPortfolioValue,
                releasedAmount);

        brokerageRepository.save(customerData.getCustomerCustodies());

        return new CustomerRebalanceOutcome(releasedAmount, buyNeed);
    }

    private record CustomerRebalanceOutcome(Money releasedAmount, CustomerBuyNeedDTO buyNeed) {
    }

    private RebalanceResultDTO soldRemovedPositionsFromMaster(
            BrokerageAccount masterAccount,
            List<String> removedTickers) {

        Money releasedAmount = Money.create(BigDecimal.ZERO);
        List<PortfolioRebalance> rebalances = new ArrayList<>();

        for (Custody custody : masterAccount.getCustodies()) {
            if (removedTickers.contains(custody.getTicker())) {
                Money custodyValue = custody.getAveragePrice()
                        .multiply(BigDecimal.valueOf(custody.getQuantity()));

                releasedAmount = releasedAmount.add(custodyValue);

                custody.sellAllQuantity();

                rebalances.add(PortfolioRebalance.create(
                        masterAccount.getCustomer().getId(),
                        RebalanceType.PORTFOLIO_CHANGE,
                        custody.getTicker(),
                        "",
                        custodyValue
                ));
            }
        }

        if (!rebalances.isEmpty()) {
            portfolioRebalanceRepository.saveAndFlushAll(rebalances);
        }

        return new RebalanceResultDTO(releasedAmount, rebalances);
    }

}
