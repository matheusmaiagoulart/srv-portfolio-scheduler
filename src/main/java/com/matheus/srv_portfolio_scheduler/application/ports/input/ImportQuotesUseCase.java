package com.matheus.srv_portfolio_scheduler.application.ports.input;

import com.matheus.srv_portfolio_scheduler.application.command.ImportQuotes.ImportQuotesCommand;
import com.matheus.srv_portfolio_scheduler.application.dto.QuoteDTO;

import java.util.List;

public interface ImportQuotesUseCase {
    List<QuoteDTO> handler(ImportQuotesCommand importQuotesCommand);
}
