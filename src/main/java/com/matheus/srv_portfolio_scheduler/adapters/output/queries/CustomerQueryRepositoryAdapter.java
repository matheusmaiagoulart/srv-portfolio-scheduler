package com.matheus.srv_portfolio_scheduler.adapters.output.queries;

import com.matheus.srv_portfolio_scheduler.adapters.mapper.CustomerMapper;
import com.matheus.srv_portfolio_scheduler.application.ports.output.queries.AssetPriceQueryRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.ports.output.queries.CustomerQueryRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.queries.GetCustomerPortfolio.GetCustomerPortfolioResponse;
import com.matheus.srv_portfolio_scheduler.domain.entities.Custody;
import com.matheus.srv_portfolio_scheduler.domain.entities.Customer;
import com.matheus.srv_portfolio_scheduler.domain.exceptions.CustomerNotFound;
import com.matheus.srv_portfolio_scheduler.domain.services.PortfolioAnalytics;
import com.matheus.srv_portfolio_scheduler.domain.valueObject.Money;
import com.matheus.srv_portfolio_scheduler.infrastructure.queries.JpaCustomerQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomerQueryRepositoryAdapter implements CustomerQueryRepositoryPort {

    private final JpaCustomerQueryRepository customerRepository;
    private final AssetPriceQueryRepositoryPort assetPriceRepository;

    @Override
    public GetCustomerPortfolioResponse getCustomerPortfolio(long customerId) {

        Customer customer = customerRepository.getCustomerWithCustodiesById(customerId)
                .map(CustomerMapper::toDomain)
                .orElseThrow(() -> new CustomerNotFound(customerId));

        List<String> tickers = customer.getBrokerageAccount().getCustodies().stream()
                .map(Custody::getTicker)
                .toList();

        Map<String, Money> currentPrices = assetPriceRepository.getAssetsPrices(tickers);

        List<Custody> custodies = customer.getBrokerageAccount().getCustodies();

        Money plTotal = PortfolioAnalytics.calcPlTotal(custodies, currentPrices);
        Money totalAmountInvested = PortfolioAnalytics.calcTotalAmountInvested(custodies);
        Money portfolioCurrentValue = PortfolioAnalytics.calcPortfolioTotalValue(custodies, currentPrices);
        BigDecimal plPercentage = PortfolioAnalytics.calcPortfolioProfitability(portfolioCurrentValue, totalAmountInvested);

        return new GetCustomerPortfolioResponse(
                customerId,
                customer.getName(),
                customer.getBrokerageAccount().getId(),
                LocalDateTime.now(),
                new GetCustomerPortfolioResponse.Resume(
                        totalAmountInvested,
                        portfolioCurrentValue,
                        plTotal,
                        plPercentage
                ),
                new GetCustomerPortfolioResponse.Assets(
                        custodies.stream()
                                .map(custody -> new GetCustomerPortfolioResponse.AssetsDetails(
                                        custody.getTicker(),
                                        custody.getQuantity(),
                                        custody.getAveragePrice(),
                                        Money.create(currentPrices.get(custody.getTicker()).getAmount()),
                                        custody.calcPl(currentPrices.get(custody.getTicker())),
                                        custody.calcPlPercentual(currentPrices.get(custody.getTicker())),
                                        custody.calcCompositionPercentage(currentPrices.get(custody.getTicker()), portfolioCurrentValue)
                                ))
                                .toList()));
    }
}
