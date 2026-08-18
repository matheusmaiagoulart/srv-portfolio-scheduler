package com.matheus.srv_portfolio_scheduler.application.command.PortfolioRebalance;

import com.matheus.srv_portfolio_scheduler.domain.services.dto.QuoteDTO;
import com.matheus.srv_portfolio_scheduler.application.ports.input.commands.PortfolioRebalanceUseCase;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.BrokerageAccountRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.CotahistFilePort;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.RecommendedPortfolioRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.service.PortfolioPurchaseExecutionService;
import com.matheus.srv_portfolio_scheduler.application.service.PortfolioRebalanceService;
import com.matheus.srv_portfolio_scheduler.domain.entities.BrokerageAccount;
import com.matheus.srv_portfolio_scheduler.domain.entities.RecommendedPortfolio;
import com.matheus.srv_portfolio_scheduler.domain.exceptions.ActivePortfolioNotFoundException;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.RebalanceExecutionResultDTO;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioRebalanceHandler implements PortfolioRebalanceUseCase {

    private final CotahistFilePort cotahistFilePort;
    private final PortfolioRebalanceService portfolioRebalanceService;
    private final BrokerageAccountRepositoryPort brokerageAccountRepository;
    private final RecommendedPortfolioRepositoryPort portfolioRepository;
    private final PortfolioPurchaseExecutionService portfolioPurchaseExecutionService;

    @Override
    // readOnly: garante uma sessão Hibernate ativa durante TODO o metodo,
    // necessária pois o handler agora roda em thread assíncrona (@Async listener),
    // sem sessão herdada, e acessa colecoes lazy (ex: recommendedPortfolio.getPortfolioItems()).
    // As chamadas internas com REQUIRES_NEW suspendem esta transação normalmente.
    @Transactional(readOnly = true)
    public PortfolioRebalanceResponse handler(PortfolioRebalanceCommand command) {

        RecommendedPortfolio recommendedPortfolio = portfolioRepository.getActiveRecommendedPortfolio()
                .orElseThrow(ActivePortfolioNotFoundException::new);

        if (!command.portfolioComparisonDTO().hasChange()) return new PortfolioRebalanceResponse();

        Set<String> allTickers = command.portfolioComparisonDTO().getAllTickersName();

        // Tickers que NÃO mudaram de percentual (ex: WEGE3 15%->15%) não aparecem
        // em altered/removed/added, mas ainda assim precisam de preço, pois entram
        // no cálculo do valor total do portfólio de cada cliente (SellExcessOperation
        // e CalculateBuyNeedOperation somam TODAS as custódias, não só as alteradas).
        recommendedPortfolio.getPortfolioItems().forEach(item -> allTickers.add(item.getTicker()));

        List<QuoteDTO> quotes = cotahistFilePort.getTickerByLastCotahist(allTickers);

        Map<String, Money> pricesByTicker = quotes.stream()
                .collect(Collectors.toMap(
                        QuoteDTO::ticker,
                        quote -> Money.create(quote.closePrice())
                ));

        BrokerageAccount masterAccount = brokerageAccountRepository.getMasterAccount();

        log.info("Starting process of updating customers custodies to sold and update positions for master account: {}", masterAccount.getId());
        // Starting process of updating customers custodies to sold and update positions,
        // also calculating each customer's individual buy need for the new portfolio
        RebalanceExecutionResultDTO rebalanceResult = portfolioRebalanceService.execute(
                masterAccount,
                command.portfolioComparisonDTO(),
                pricesByTicker
        );

        log.info("Starting process of purchase and distribution");
        portfolioPurchaseExecutionService.executePurchase(
                recommendedPortfolio,
                masterAccount,
                rebalanceResult,
                quotes
        );


        return new PortfolioRebalanceResponse();
    }
}