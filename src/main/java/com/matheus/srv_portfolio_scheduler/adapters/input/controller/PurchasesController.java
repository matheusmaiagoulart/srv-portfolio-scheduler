package com.matheus.srv_portfolio_scheduler.adapters.input.controller;

import com.matheus.srv_portfolio_scheduler.adapters.input.swagger.SwaggerPurchasesController;
import com.matheus.srv_portfolio_scheduler.application.command.ExecutePortfolioPurchase.ExecutePortfolioPurchaseCommand;
import com.matheus.srv_portfolio_scheduler.application.command.ExecutePortfolioPurchase.ExecutePortfolioPurchaseResponse;
import com.matheus.srv_portfolio_scheduler.application.command.ImportQuotes.ImportQuotesCommand;
import com.matheus.srv_portfolio_scheduler.application.ports.input.commands.ExecutePortfolioPurchaseUseCase;
import com.matheus.srv_portfolio_scheduler.application.ports.input.commands.ImportQuotesUseCase;
import com.matheus.srv_portfolio_scheduler.domain.services.dto.QuoteDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(value = "v1/api/purchases")
public class PurchasesController implements SwaggerPurchasesController {

    private final ImportQuotesUseCase importQuotesUseCase;
    private final ExecutePortfolioPurchaseUseCase executePortfolioPurchaseUseCase;

    @PostMapping("execute-purchase")
    public ResponseEntity<ExecutePortfolioPurchaseResponse> executePurchase(@RequestBody ImportQuotesCommand command) {

        List<QuoteDTO> result = importQuotesUseCase.handler(command);

        var resultPurchase = executePortfolioPurchaseUseCase.handler(new ExecutePortfolioPurchaseCommand(result));

        return ResponseEntity.status(201).body(resultPurchase);
    }
}
