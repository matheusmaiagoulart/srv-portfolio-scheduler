package com.matheus.srv_portfolio_scheduler.adapters.input.swagger;

import com.matheus.srv_portfolio_scheduler.application.command.ExecutePortfolioPurchase.ExecutePortfolioPurchaseResponse;
import com.matheus.srv_portfolio_scheduler.application.command.ImportQuotes.ImportQuotesCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Purchases", description = "Operações relacionadas à execução do ciclo automático de compra de ações.")
public interface SwaggerPurchasesController {

    @Operation(
            summary = "Execução do ciclo de compra automatizada",
            description = """
                    Executa o ciclo completo de compra automatizada de ações para todos os clientes assinantes ativos. \
                    O processo realiza as seguintes etapas em sequência:
                    1. Importa as cotações do arquivo COTAHIST fornecido (formato B3);
                    2. Identifica a cesta de ações recomendada vigente;
                    3. Calcula a quantidade de ações a comprar para cada cliente com base no saldo disponível;
                    4. Registra as ordens de compra e gera os eventos de entrega;
                    5. Invalida o cache de carteira de todos os clientes afetados.
                    Este endpoint é tipicamente chamado pelo scheduler interno a cada 10 dias.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ciclo de compra executado com sucesso.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExecutePortfolioPurchaseResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados do arquivo de cotações inválidos ou ausentes.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Erro interno durante o processamento da compra.",
                    content = @Content(mediaType = "application/json"))
    })
    ResponseEntity<ExecutePortfolioPurchaseResponse> executePurchase(ImportQuotesCommand command);
}
