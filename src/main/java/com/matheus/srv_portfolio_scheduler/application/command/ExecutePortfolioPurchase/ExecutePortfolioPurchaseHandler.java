package com.matheus.srv_portfolio_scheduler.application.command.ExecutePortfolioPurchase;

import com.matheus.srv_portfolio_scheduler.adapters.utils.CorrelationId;
import com.matheus.srv_portfolio_scheduler.application.ports.input.commands.ExecutePortfolioPurchaseUseCase;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.BrokerageAccountRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.CustomerRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.PurchaseOrderRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.RecommendedPortfolioRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.service.ProcessDistributionInBatch;
import com.matheus.srv_portfolio_scheduler.domain.entities.BrokerageAccount;
import com.matheus.srv_portfolio_scheduler.domain.entities.PurchaseOrder;
import com.matheus.srv_portfolio_scheduler.domain.entities.RecommendedPortfolio;
import com.matheus.srv_portfolio_scheduler.domain.exceptions.ActivePortfolioNotFoundException;
import com.matheus.srv_portfolio_scheduler.domain.exceptions.CotahistNotFoundException;
import com.matheus.srv_portfolio_scheduler.domain.exceptions.MasterAccountNotFoundException;
import com.matheus.srv_portfolio_scheduler.domain.services.PurchaseQuotesCalculator;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.AssetPurchaseDTO;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.DistributionsResultDTO;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutePortfolioPurchaseHandler implements ExecutePortfolioPurchaseUseCase {

    private final CustomerRepositoryPort customerRepository;
    private final ProcessDistributionInBatch distributionInBatch;
    private final PurchaseQuotesCalculator purchaseQuotesCalculator;
    private final BrokerageAccountRepositoryPort brokerageRepository;
    private final PurchaseOrderRepositoryPort purchaseOrderRepository;
    private final RecommendedPortfolioRepositoryPort recommendedPortfolioRepositoryPort;

    @Override
    @Transactional
    public ExecutePortfolioPurchaseResponse handler(ExecutePortfolioPurchaseCommand command) {

        log.info("Starting execution of portfolio purchase.",
                kv("correlationId", CorrelationId.get()),
                kv("date", LocalDate.now().toString()));

        if (command.lastCotahist().isEmpty()) throw new CotahistNotFoundException();

        final RecommendedPortfolio portfolio = recommendedPortfolioRepositoryPort.getActiveRecommendedPortfolio()
                .orElseThrow(ActivePortfolioNotFoundException::new);

        BrokerageAccount masterAccount = brokerageRepository.getMasterAccount()
                .orElseThrow(MasterAccountNotFoundException::new);

        Money thirdValueOfAllCustomers = customerRepository.getThirdAmountOfAllActiveCustomers();

        Map<String, Money> amountPerAsset = purchaseQuotesCalculator
                .calculateAmountPerAsset(thirdValueOfAllCustomers, portfolio);

        Map<String, AssetPurchaseDTO> calculateQuantityPerAsset = purchaseQuotesCalculator.calculateQuantityPerAsset(
                amountPerAsset,
                command.lastCotahist(),
                masterAccount
        );

        List<PurchaseOrder> purchaseOrders = PurchaseOrder.createPurchaseOrders(
                masterAccount.getId(), calculateQuantityPerAsset.values().stream().toList());

        purchaseOrders = purchaseOrderRepository.save(purchaseOrders);

        DistributionsResultDTO resultDistribution = distributionInBatch.processInBatch(
                purchaseOrders,
                thirdValueOfAllCustomers,
                masterAccount);

        return ExecutePortfolioPurchaseResponse.buildResponse(resultDistribution);
    }
}
