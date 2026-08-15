package com.matheus.srv_portfolio_scheduler.application.ports.output.commands;

import com.matheus.srv_portfolio_scheduler.domain.services.dto.QuoteDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CotahistFilePort {
    List<QuoteDTO> parse (Set<String> tickers, String pathFile);
    List<QuoteDTO> getTickerByLastCotahist (Set<String> tickers);
    Optional<String> existsCotahistFile (LocalDate referenceDate);
}
