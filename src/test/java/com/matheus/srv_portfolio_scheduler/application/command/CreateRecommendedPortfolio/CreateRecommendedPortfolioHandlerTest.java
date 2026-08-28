package com.matheus.srv_portfolio_scheduler.application.command.CreateRecommendedPortfolio;

import com.matheus.srv_portfolio_scheduler.application.ports.output.commands.RecommendedPortfolioRepositoryPort;
import com.matheus.srv_portfolio_scheduler.domain.entities.RecommendedPortfolio;
import com.matheus.srv_portfolio_scheduler.domain.services.PortfolioComparisonService;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.PortfolioComparisonDTO;
import com.matheus.srv_portfolio_scheduler.fixtures.RecommendedPortfolioFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateRecommendedPortfolioHandlerTest {

    @Mock
    private PortfolioComparisonService portfolioComparisonService;
    @Mock
    private RecommendedPortfolioRepositoryPort recommendedPortfolioRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CreateRecommendedPortfolioHandler handler;

    @Test
    void deveCriarPrimeiraCarteiraSemAcionarRebalanceamento() {
        when(recommendedPortfolioRepository.getActiveRecommendedPortfolio()).thenReturn(Optional.empty());

        CreateRecommendedPortfolioResponse response = handler.handler(defaultCommand());

        assertEquals("New Portfolio", response.name());
        assertTrue(response.active());
        assertEquals(5, response.portfolioItems().size());
        assertFalse(response.rebalanceTriggered());
        verify(recommendedPortfolioRepository).save(any(RecommendedPortfolio.class));
        verifyNoInteractions(portfolioComparisonService, eventPublisher);
    }

    @Test
    void deveDesativarCarteiraAtualQuandoNaoHouverMudanca() {
        RecommendedPortfolio currentPortfolio = RecommendedPortfolioFixture.aRecommendedPortfolio().build();
        PortfolioComparisonDTO noChanges = new PortfolioComparisonDTO(List.of(), List.of(), List.of());
        when(recommendedPortfolioRepository.getActiveRecommendedPortfolio()).thenReturn(Optional.of(currentPortfolio));
        when(portfolioComparisonService.compare(any(), any())).thenReturn(noChanges);

        CreateRecommendedPortfolioResponse response = handler.handler(defaultCommand());

        assertFalse(currentPortfolio.isActive());
        assertFalse(response.rebalanceTriggered());
        verify(recommendedPortfolioRepository, times(2)).save(any(RecommendedPortfolio.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void devePublicarEventoDeRebalanceamentoQuandoCarteiraMudar() {
        RecommendedPortfolio currentPortfolio = RecommendedPortfolioFixture.aRecommendedPortfolio().build();
        PortfolioComparisonDTO changes = new PortfolioComparisonDTO(
                List.of(new PortfolioComparisonDTO.AlteredItem("PETR4", new BigDecimal("20"), new BigDecimal("25"))),
                List.of(),
                List.of());
        when(recommendedPortfolioRepository.getActiveRecommendedPortfolio()).thenReturn(Optional.of(currentPortfolio));
        when(portfolioComparisonService.compare(any(), any())).thenReturn(changes);

        CreateRecommendedPortfolioResponse response = handler.handler(defaultCommand());

        assertTrue(response.rebalanceTriggered());
        assertFalse(currentPortfolio.isActive());
        verify(eventPublisher).publishEvent(any(Object.class));
    }

    private CreateRecommendedPortfolioCommand defaultCommand() {
        List<PortfolioItemCommand> items = RecommendedPortfolioFixture.defaultItems().stream()
                .map(item -> new PortfolioItemCommand(item.getTicker(), item.getPercentage()))
                .toList();
        return new CreateRecommendedPortfolioCommand("New Portfolio", items, null);
    }
}