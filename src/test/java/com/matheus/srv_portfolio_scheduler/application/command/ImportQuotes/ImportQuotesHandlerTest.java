package com.matheus.srv_portfolio_scheduler.application.command.ImportQuotes;

import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.AssetPriceRepositoryPort;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.CotahistFilePort;
import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.RecommendedPortfolioRepositoryPort;
import com.matheus.srv_portfolio_scheduler.domain.entities.RecommendedPortfolio;
import com.matheus.srv_portfolio_scheduler.domain.exceptions.ActivePortfolioNotFoundException;
import com.matheus.srv_portfolio_scheduler.domain.exceptions.BusinessException;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.QuoteDTO;
import com.matheus.srv_portfolio_scheduler.fixtures.QuoteFixture;
import com.matheus.srv_portfolio_scheduler.fixtures.RecommendedPortfolioFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportQuotesHandlerTest {

    private static final LocalDate REFERENCE_DATE = LocalDate.of(2026, 3, 27);
    private static final String FILE_PATH = "cotahist/COTAHIST_D27032026.TXT";
    private static final Set<String> PORTFOLIO_TICKERS =
            Set.of("PETR4", "VALE3", "ITUB4", "BBDC4", "WEGE3");

    @Mock
    private CotahistFilePort cotahistFilePort;
    @Mock
    private AssetPriceRepositoryPort assetPriceRepository;
    @Mock
    private RecommendedPortfolioRepositoryPort recommendedPortfolioRepository;

    @InjectMocks
    private ImportQuotesHandler handler;

    @Test
    void deveImportarCotacoesDosAtivosDaCarteiraAtiva() {
        // Arrange
        RecommendedPortfolio portfolio = RecommendedPortfolioFixture.aRecommendedPortfolio().build();
        QuoteDTO quote = QuoteFixture.aQuote().withTradingDate(REFERENCE_DATE).build();

        when(cotahistFilePort.existsCotahistFile(REFERENCE_DATE)).thenReturn(Optional.of(FILE_PATH));
        when(recommendedPortfolioRepository.getActiveRecommendedPortfolio()).thenReturn(Optional.of(portfolio));
        when(cotahistFilePort.parse(eq(PORTFOLIO_TICKERS), eq(FILE_PATH))).thenReturn(List.of(quote));

        // Act
        List<QuoteDTO> result = handler.handler(new ImportQuotesCommand(REFERENCE_DATE));

        // Assert
        assertEquals(1, result.size());
        assertSame(quote, result.getFirst());
        verify(assetPriceRepository).saveAll(anyList());
    }

    @Test
    void deveLancarExcecaoQuandoArquivoNaoExistir() {
        // Arrange
        when(cotahistFilePort.existsCotahistFile(REFERENCE_DATE)).thenReturn(Optional.empty());

        // Act + Assert
        BusinessException exception = assertThrows(BusinessException.class,
                () -> handler.handler(new ImportQuotesCommand(REFERENCE_DATE)));

        assertEquals("COTAHIST_NOT_FOUND", exception.getErrorCode());
        verifyNoInteractions(recommendedPortfolioRepository, assetPriceRepository);
    }

    @Test
    void deveLancarExcecaoQuandoCarteiraAtivaNaoExistir() {
        // Arrange
        when(cotahistFilePort.existsCotahistFile(REFERENCE_DATE)).thenReturn(Optional.of(FILE_PATH));
        when(recommendedPortfolioRepository.getActiveRecommendedPortfolio()).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(ActivePortfolioNotFoundException.class,
                () -> handler.handler(new ImportQuotesCommand(REFERENCE_DATE)));

        verifyNoInteractions(assetPriceRepository);
    }
}

