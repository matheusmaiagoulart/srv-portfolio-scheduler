package com.matheus.srv_portfolio_scheduler.application.queries.GetMasterCustody;

import java.math.BigDecimal;
import java.util.List;

public record GetMasterCustodyResponse(
        MasterAccount masterAccount,
        List<CustodyItem> custody,
        BigDecimal totalResidualValue
) {
    public record MasterAccount(
            Long id,
            String accountNumber,
            String type
    ) {
    }

    public record CustodyItem(
            String ticker,
            int quantity,
            BigDecimal averagePrice,
            BigDecimal currentPrice,
            String origin
    ) {
    }
}
