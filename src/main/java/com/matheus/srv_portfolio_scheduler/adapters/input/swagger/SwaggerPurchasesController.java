package com.matheus.srv_portfolio_scheduler.adapters.input.swagger;

import com.matheus.srv_portfolio_scheduler.application.command.ImportQuotes.ImportQuotesCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "PurchasesController")
public interface SwaggerPurchasesController {

    @Operation(summary = "Execução da Compra Automatizada",
            description = "Executa a compra automatizada de ações com base nas cotações importadas e " +
                    "na carteira recomendada.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Ok")})
    ResponseEntity executePurchase(ImportQuotesCommand command);
}
