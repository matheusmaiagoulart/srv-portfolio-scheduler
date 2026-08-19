package com.matheus.srv_portfolio_scheduler.application.listener;

import com.matheus.srv_portfolio_scheduler.application.command.PortfolioRebalance.PortfolioRebalanceCommand;
import com.matheus.srv_portfolio_scheduler.application.event.PortfolioRebalanceRequestedEvent;
import com.matheus.srv_portfolio_scheduler.application.ports.input.commands.PortfolioRebalanceUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PortfolioRebalanceEventListener {

    private final PortfolioRebalanceUseCase portfolioRebalanceUseCase;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPortfolioRebalanceRequested(PortfolioRebalanceRequestedEvent event) {
        log.info("Portfolio rebalance event received, starting async processing");

        try {
            portfolioRebalanceUseCase.handler(new PortfolioRebalanceCommand(event.portfolioComparisonDTO()));
        } catch (Exception e) {
            log.error("Error processing async portfolio rebalance", e);
        }
    }
}

