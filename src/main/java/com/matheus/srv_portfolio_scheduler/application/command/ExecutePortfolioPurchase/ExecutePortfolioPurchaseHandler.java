package com.matheus.srv_portfolio_scheduler.application.command.ExecutePortfolioPurchase;

import com.matheus.srv_portfolio_scheduler.application.ports.output.queries.RedisCachePort;
import com.matheus.srv_portfolio_scheduler.application.utils.CorrelationId;
import com.matheus.srv_portfolio_scheduler.application.ports.input.commands.ExecutePortfolioPurchaseUseCase;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.BrokerageAccountRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.CustomerRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.RecommendedPortfolioRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.service.PortfolioPurchaseExecutionService;
import com.matheus.srv_portfolio_scheduler.domain.entities.BrokerageAccount;
import com.matheus.srv_portfolio_scheduler.domain.entities.RecommendedPortfolio;
import com.matheus.srv_portfolio_scheduler.domain.exceptions.ActivePortfolioNotFoundException;
import com.matheus.srv_portfolio_scheduler.domain.exceptions.CotahistNotFoundException;
import com.matheus.srv_portfolio_scheduler.domain.services.PurchaseExecutionDateValidator;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.PurchaseSummaryDTO;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutePortfolioPurchaseHandler implements ExecutePortfolioPurchaseUseCase {

    private final RedisCachePort redisCachePort;
    private final CustomerRepositoryPort customerRepository;
    private final BrokerageAccountRepositoryPort brokerageRepository;
    private final PurchaseExecutionDateValidator purchaseExecutionDateValidator;
    private final PortfolioPurchaseExecutionService portfolioPurchaseExecutionService;
    private final RecommendedPortfolioRepositoryPort recommendedPortfolioRepositoryPort;

    @Override
    @Transactional
    public ExecutePortfolioPurchaseResponse handler(ExecutePortfolioPurchaseCommand command) {

        log.info("Starting execution of portfolio purchase.",
                kv("correlationId", CorrelationId.get()),
                kv("date", LocalDate.now().toString()));

        purchaseExecutionDateValidator.validate(LocalDate.now());

        if (command.lastCotahist().isEmpty()) throw new CotahistNotFoundException();

        final RecommendedPortfolio portfolio = recommendedPortfolioRepositoryPort
                .getActiveRecommendedPortfolio()
                .orElseThrow(ActivePortfolioNotFoundException::new);

        BrokerageAccount masterAccount = brokerageRepository.getMasterAccount();
        Money thirdValueOfAllCustomers = customerRepository.getThirdAmountOfAllActiveCustomers();

        redisCachePort.invalidateCacheForCustomersPortfolios();

        PurchaseSummaryDTO resultDistribution = portfolioPurchaseExecutionService.executePurchase(
                portfolio,
                masterAccount,
                thirdValueOfAllCustomers,
                command.lastCotahist()
        );

        return ExecutePortfolioPurchaseResponse.buildResponse(resultDistribution);
    }
}
