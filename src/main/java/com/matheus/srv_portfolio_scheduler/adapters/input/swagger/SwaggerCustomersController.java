package com.matheus.srv_portfolio_scheduler.adapters.input.swagger;

import com.matheus.srv_portfolio_scheduler.application.command.RegisterCustomerSubscriber.RegisterCustomerSubscriberCommand;
import com.matheus.srv_portfolio_scheduler.application.command.RegisterCustomerSubscriber.RegisterCustomerSubscriberResponse;
import com.matheus.srv_portfolio_scheduler.application.queries.GetCustomerPortfolio.GetCustomerPortfolioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Customers", description = "Operações relacionadas ao cadastro de clientes e consulta de carteira de investimentos.")
public interface SwaggerCustomersController {

    @Operation(
            summary = "Cadastro de cliente assinante",
            description = """
                    Realiza o cadastro do cliente na plataforma de compra automatizada de ações.
                    Após o cadastro, o cliente passa a participar dos ciclos automáticos de compra \
                    realizados pela corretora a cada 10 dias, com base na cesta de ações recomendada vigente.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cliente cadastrado com sucesso.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RegisterCustomerSubscriberResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos ou ausentes.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "Cliente já cadastrado na plataforma.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor.",
                    content = @Content(mediaType = "application/json"))
    })
    ResponseEntity<RegisterCustomerSubscriberResponse> registerCustomerSubscriber(
            @RequestBody @Valid RegisterCustomerSubscriberCommand command);

    @Operation(
            summary = "Consulta de carteira do cliente",
            description = """
                    Retorna a carteira de investimentos do cliente, incluindo todos os ativos que possui, \
                    quantidades, preço médio de compra, preço atual, lucro/prejuízo (P&L) por ativo e total, \
                    além da composição percentual do portfólio.
                    O resultado é cacheado por aproximadamente 10 dias e invalidado automaticamente \
                    ao final de cada ciclo de compra.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Carteira retornada com sucesso.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetCustomerPortfolioResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor.",
                    content = @Content(mediaType = "application/json"))
    })
    ResponseEntity<GetCustomerPortfolioResponse> getCustomerPortfolio(
            @Parameter(description = "ID único do cliente.", required = true, example = "22")
            @PathVariable long id);
}
