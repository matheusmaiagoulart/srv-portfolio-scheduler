package com.matheus.srv_portfolio_scheduler.adapters.input.controller;

import com.matheus.srv_portfolio_scheduler.adapters.input.swagger.SwaggerPortfoliosController;
import com.matheus.srv_portfolio_scheduler.application.command.CreateRecommendedPortfolio.CreateRecommendedPortfolioCommand;
import com.matheus.srv_portfolio_scheduler.application.command.CreateRecommendedPortfolio.CreateRecommendedPortfolioResponse;
import com.matheus.srv_portfolio_scheduler.application.ports.input.CreateRecommendedPortfolioUseCase;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping(value = "v1/api/admin/cesta")
public class PortfoliosController implements SwaggerPortfoliosController {

    private final CreateRecommendedPortfolioUseCase createRecommendedPortfolioUseCase;

    @Override
    @PostMapping
    public ResponseEntity<CreateRecommendedPortfolioResponse> createRecommendedPortfolio(
             @RequestBody @Valid CreateRecommendedPortfolioCommand request) {

        return ResponseEntity.status(201).body(createRecommendedPortfolioUseCase.handler(request));
    }
}
