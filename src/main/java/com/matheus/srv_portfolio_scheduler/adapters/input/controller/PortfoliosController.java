package com.matheus.srv_portfolio_scheduler.adapters.input.controller;

import com.matheus.srv_portfolio_scheduler.adapters.input.swagger.SwaggerPortfoliosController;
import com.matheus.srv_portfolio_scheduler.application.command.CreateRecommendedPortfolio.CreateRecommendedPortfolioCommand;
import com.matheus.srv_portfolio_scheduler.application.command.CreateRecommendedPortfolio.CreateRecommendedPortfolioResponse;
import com.matheus.srv_portfolio_scheduler.application.ports.input.commands.CreateRecommendedPortfolioUseCase;
import com.matheus.srv_portfolio_scheduler.application.ports.input.queries.GetAllRecommendedPortfoliosUseCase;
import com.matheus.srv_portfolio_scheduler.application.ports.input.queries.GetCurrentRecommendedPortfolioUseCase;
import com.matheus.srv_portfolio_scheduler.application.queries.GetAllRecommendedPortfolios.GetAllRecommendedPortfoliosQuery;
import com.matheus.srv_portfolio_scheduler.application.queries.GetAllRecommendedPortfolios.GetAllRecommendedPortfoliosResponse;
import com.matheus.srv_portfolio_scheduler.application.queries.GetCurrentRecommendedPortfolio.GetCurrentRecommendedPortfolioQuery;
import com.matheus.srv_portfolio_scheduler.application.queries.GetCurrentRecommendedPortfolio.GetCurrentRecommendedPortfolioResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(value = "v1/api/admin/cesta")
public class PortfoliosController implements SwaggerPortfoliosController {

    private final CreateRecommendedPortfolioUseCase createRecommendedPortfolioUseCase;
    private final GetAllRecommendedPortfoliosUseCase getAllRecommendedPortfoliosUseCase;
    private final GetCurrentRecommendedPortfolioUseCase getCurrentRecommendedPortfolioUseCase;

    @Override
    @PostMapping
    public ResponseEntity<CreateRecommendedPortfolioResponse> createRecommendedPortfolio(
            @RequestBody @Valid CreateRecommendedPortfolioCommand request) {

        return ResponseEntity.status(201).body(createRecommendedPortfolioUseCase.handler(request));
    }

    @Override
    @GetMapping("historico")
    public ResponseEntity<GetAllRecommendedPortfoliosResponse> getAllRecommendedPortfolios() {
        return ResponseEntity.status(200).body(getAllRecommendedPortfoliosUseCase.handler(new GetAllRecommendedPortfoliosQuery()));
    }

    @Override
    @GetMapping("atual")
    public ResponseEntity<GetCurrentRecommendedPortfolioResponse> getCurrentRecommendedPortfolio() {
        return ResponseEntity.status(200).body(getCurrentRecommendedPortfolioUseCase.handler(new GetCurrentRecommendedPortfolioQuery()));
    }
}
