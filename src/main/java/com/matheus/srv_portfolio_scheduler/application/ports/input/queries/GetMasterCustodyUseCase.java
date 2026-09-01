package com.matheus.srv_portfolio_scheduler.application.ports.input.queries;

import com.matheus.srv_portfolio_scheduler.application.queries.GetMasterCustody.GetMasterCustodyQuery;
import com.matheus.srv_portfolio_scheduler.application.queries.GetMasterCustody.GetMasterCustodyResponse;

public interface GetMasterCustodyUseCase {
    GetMasterCustodyResponse handler(GetMasterCustodyQuery query);
}
