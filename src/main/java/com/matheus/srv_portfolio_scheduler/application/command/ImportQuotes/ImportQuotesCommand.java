package com.matheus.srv_portfolio_scheduler.application.command.ImportQuotes;

import java.time.LocalDate;

public record ImportQuotesCommand(
        LocalDate referenceDate
){
}
