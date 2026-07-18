package com.matheus.srv_portfolio_scheduler.adapters.input.controller;

import com.matheus.srv_portfolio_scheduler.adapters.input.swagger.SwaggerPortfoliosController;
import com.matheus.srv_portfolio_scheduler.application.command.CreateRecommendedPortfolio.CreateRecommendedPortfolioCommand;
import com.matheus.srv_portfolio_scheduler.application.command.CreateRecommendedPortfolio.CreateRecommendedPortfolioResponse;
import com.matheus.srv_portfolio_scheduler.application.ports.input.commands.CreateRecommendedPortfolioUseCase;
import com.matheus.srv_portfolio_scheduler.application.ports.input.queries.GetAllRecommendedPortfoliosUseCase;
import com.matheus.srv_portfolio_scheduler.application.queries.GetAllRecommendedPortfolios.GetAllRecommendedPortfoliosQuery;
import com.matheus.srv_portfolio_scheduler.application.queries.GetAllRecommendedPortfolios.GetAllRecommendedPortfoliosResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(value = "v1/api/admin/cesta")
public class PortfoliosController implements SwaggerPortfoliosController {

    private final CreateRecommendedPortfolioUseCase createRecommendedPortfolioUseCase;
    private final GetAllRecommendedPortfoliosUseCase getAllRecommendedPortfoliosUseCase;

    @Override
    @PostMapping
    public ResponseEntity<CreateRecommendedPortfolioResponse> createRecommendedPortfolio(
             @RequestBody @Valid CreateRecommendedPortfolioCommand request) {

        return ResponseEntity.status(201).body(createRecommendedPortfolioUseCase.handler(request));
    }

    @Override
    @GetMapping("historico")
    public GetAllRecommendedPortfoliosResponse getAllRecommendedPortfolios() {
        return getAllRecommendedPortfoliosUseCase.handler(new GetAllRecommendedPortfoliosQuery());
    }
}
