package com.matheus.srv_portfolio_scheduler.application.ports.output.commands;

import com.matheus.srv_portfolio_scheduler.application.dto.QuoteDTO;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public interface CotahistFilePort {
    List<QuoteDTO> parse (HashSet<String> tickers, String pathFile);
    List<QuoteDTO> getTickerByLastCotahist (HashSet<String> tickers);
    Optional<String> existsCotahistFile (LocalDate referenceDate);
}
