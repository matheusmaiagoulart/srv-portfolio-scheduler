package com.matheus.srv_portfolio_scheduler.application.service;

import com.matheus.srv_portfolio_scheduler.domain.services.dto.QuoteDTO;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.PurchaseOrderRepositoryPort;
import com.matheus.srv_portfolio_scheduler.domain.entities.BrokerageAccount;
import com.matheus.srv_portfolio_scheduler.domain.entities.PurchaseOrder;
import com.matheus.srv_portfolio_scheduler.domain.entities.RecommendedPortfolio;
import com.matheus.srv_portfolio_scheduler.domain.services.PurchaseQuotesCalculator;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.AssetPurchaseDTO;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.PurchaseSummaryDTO;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.RebalanceExecutionResultDTO;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioPurchaseExecutionService {

    private final ProcessDistributionInBatch distributionInBatch;
    private final PurchaseQuotesCalculator purchaseQuotesCalculator;
    private final PurchaseOrderRepositoryPort purchaseOrderRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PurchaseSummaryDTO executePurchase(
            RecommendedPortfolio portfolio,
            BrokerageAccount masterAccount,
            Money totalAmount,
            List<QuoteDTO> lastCotahist
    ) {
        Map<String, Money> amountPerAsset = purchaseQuotesCalculator
                .calculateAmountPerAsset(totalAmount, portfolio);

        Map<String, AssetPurchaseDTO> calculateQuantityPerAsset = purchaseQuotesCalculator
                .calculateQuantityPerAsset(amountPerAsset, lastCotahist, masterAccount);

        List<PurchaseOrder> purchaseOrders = PurchaseOrder.createPurchaseOrders(
                masterAccount.getId(),
                calculateQuantityPerAsset.values().stream().toList()
        );

        purchaseOrders = purchaseOrderRepository.save(purchaseOrders);

        return distributionInBatch.processInBatch(
                purchaseOrders,
                totalAmount,
                masterAccount
        );
    }

    /**
     * Variante usada no fluxo de rebalanceamento: em vez de aplicar a % da
     * cesta nova sobre um pool total, usa a necessidade de compra EXATA de
     * cada cliente (calculada durante a venda), agregada por ticker, e
     * distribui de volta exatamente conforme essa necessidade.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PurchaseSummaryDTO executePurchase(
            RecommendedPortfolio portfolio,
            BrokerageAccount masterAccount,
            RebalanceExecutionResultDTO rebalanceResult,
            List<QuoteDTO> lastCotahist
    ) {
        Map<String, Money> amountPerAsset = rebalanceResult.aggregatedNeedPerTicker();

        if (amountPerAsset.isEmpty()) {
            log.info("No buy needs after rebalance. Skipping purchase/distribution.");
            return new PurchaseSummaryDTO(List.of(), List.of(), 0, 0, 0);
        }

        Map<String, AssetPurchaseDTO> calculateQuantityPerAsset = purchaseQuotesCalculator
                .calculateQuantityPerAsset(amountPerAsset, lastCotahist, masterAccount);

        List<PurchaseOrder> purchaseOrders = PurchaseOrder.createPurchaseOrders(
                masterAccount.getId(),
                calculateQuantityPerAsset.values().stream().toList()
        );

        purchaseOrders = purchaseOrderRepository.save(purchaseOrders);

        return distributionInBatch.processIndividualDistribution(
                purchaseOrders,
                rebalanceResult,
                masterAccount
        );
    }
}
