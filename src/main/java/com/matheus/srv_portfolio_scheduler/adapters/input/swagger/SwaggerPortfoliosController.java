package com.matheus.srv_portfolio_scheduler.adapters.input.swagger;

import com.matheus.srv_portfolio_scheduler.application.command.CreateRecommendedPortfolio.CreateRecommendedPortfolioCommand;
import com.matheus.srv_portfolio_scheduler.application.command.CreateRecommendedPortfolio.CreateRecommendedPortfolioResponse;
import com.matheus.srv_portfolio_scheduler.application.queries.GetAllRecommendedPortfolios.GetAllRecommendedPortfoliosResponse;
import com.matheus.srv_portfolio_scheduler.application.queries.GetCurrentRecommendedPortfolio.GetCurrentRecommendedPortfolioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Portfolios", description = "Operações administrativas para criação e consulta de cestas de ações recomendadas pela corretora.")
public interface SwaggerPortfoliosController {

    @Operation(
            summary = "Criação de cesta de ações recomendadas",
            description = """
                    Cria uma nova cesta com 5 ações recomendadas pela corretora. \
                    A cesta vigente é utilizada como referência nos ciclos automáticos de compra, \
                    determinando quais ativos serão adquiridos para cada cliente assinante. \
                    Apenas uma cesta é considerada ativa por vez — a mais recente criada.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cesta criada com sucesso.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CreateRecommendedPortfolioResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos ou ausentes.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor.",
                    content = @Content(mediaType = "application/json"))
    })
    ResponseEntity<CreateRecommendedPortfolioResponse> createRecommendedPortfolio(
            @Valid @RequestBody CreateRecommendedPortfolioCommand request);

    @Operation(
            summary = "Histórico de cestas recomendadas",
            description = """
                    Retorna o histórico completo de todas as cestas de ações recomendadas \
                    que já foram criadas pela corretora, ordenadas da mais recente para a mais antiga. \
                    Útil para auditoria e acompanhamento da evolução das recomendações ao longo do tempo.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Histórico retornado com sucesso.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetAllRecommendedPortfoliosResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor.",
                    content = @Content(mediaType = "application/json"))
    })
    ResponseEntity<GetAllRecommendedPortfoliosResponse> getAllRecommendedPortfolios();

    @Operation(
            summary = "Cesta atual de ações recomendadas",
            description = "Retorna a cesta de ações recomendadas atualmente vigente."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cesta atual retornada com sucesso.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetCurrentRecommendedPortfolioResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor.",
                    content = @Content(mediaType = "application/json"))
    })
    ResponseEntity<GetCurrentRecommendedPortfolioResponse> getCurrentRecommendedPortfolio();
}
