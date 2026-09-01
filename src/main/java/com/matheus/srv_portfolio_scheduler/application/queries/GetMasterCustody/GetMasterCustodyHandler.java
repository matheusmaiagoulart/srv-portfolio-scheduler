package com.matheus.srv_portfolio_scheduler.application.queries.GetMasterCustody;

import com.matheus.srv_portfolio_scheduler.application.ports.input.queries.GetMasterCustodyUseCase;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.BrokerageAccountRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.ports.output.queries.AssetPriceQueryRepositoryPort;
import com.matheus.srv_portfolio_scheduler.domain.entities.Custody;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GetMasterCustodyHandler implements GetMasterCustodyUseCase {

    private final BrokerageAccountRepositoryPort brokerageAccountRepositoryPort;
    private final AssetPriceQueryRepositoryPort assetPriceQueryRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public GetMasterCustodyResponse handler(GetMasterCustodyQuery query) {
        var masterAccount = brokerageAccountRepositoryPort.getMasterAccount();

        List<Custody> masterCustodies = masterAccount.getCustodies().stream()
                .filter(custody -> custody.getQuantity() > 0)
                .toList();

        Map<String, Money> currentPrices = masterCustodies.isEmpty()
                ? Map.of()
                : assetPriceQueryRepositoryPort.getAssetsPrices(
                masterCustodies.stream().map(Custody::getTicker).toList()
        );

        List<GetMasterCustodyResponse.CustodyItem> custodyItems = masterCustodies.stream()
                .map(custody -> new GetMasterCustodyResponse.CustodyItem(
                        custody.getTicker(),
                        custody.getQuantity(),
                        custody.getAveragePrice().getAmount(),
                        currentPrices.get(custody.getTicker()).getAmount(),
                        "Distribution residual " + custody.getLastUpdate().toLocalDate()
                ))
                .toList();

        BigDecimal totalResidualValue = custodyItems.stream()
                .map(item -> item.currentPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        return new GetMasterCustodyResponse(
                new GetMasterCustodyResponse.MasterAccount(
                        masterAccount.getId(),
                        masterAccount.getAccountNumber(),
                        masterAccount.getAccountType().name()
                ),
                custodyItems,
                totalResidualValue
        );
    }
}
