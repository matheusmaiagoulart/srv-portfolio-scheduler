package com.matheus.srv_portfolio_scheduler.adapters.input.swagger;

import com.matheus.srv_portfolio_scheduler.application.command.CreateRecommendedPortfolio.CreateRecommendedPortfolioCommand;
import com.matheus.srv_portfolio_scheduler.application.command.CreateRecommendedPortfolio.CreateRecommendedPortfolioResponse;
import com.matheus.srv_portfolio_scheduler.application.queries.GetAllRecommendedPortfolios.GetAllRecommendedPortfoliosResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "PortfoliosController")
public interface SwaggerPortfoliosController {

    @Operation(summary = "Criação de cesta",
            description = "Cria uma cesta com 5 ações recomendadas pela corretora, " +
                    "que vão ser referênica para compras automatizadas")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Ok")})
    ResponseEntity<CreateRecommendedPortfolioResponse> createRecommendedPortfolio(
            @Valid @RequestBody CreateRecommendedPortfolioCommand request);

    @Operation(summary = "Consulta do histórico de cestas",
            description = "Retorna o histórico de cestas com 5 ações recomendadas pela corretora, " +
                    "que já foram criadas.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Ok")})
    GetAllRecommendedPortfoliosResponse getAllRecommendedPortfolios();
}
