package com.matheus.srv_portfolio_scheduler.adapters.input.controller;

import com.matheus.srv_portfolio_scheduler.adapters.input.swagger.SwaggerCustomersController;
import com.matheus.srv_portfolio_scheduler.application.command.RegisterCustomerSubscriber.RegisterCustomerSubscriberCommand;
import com.matheus.srv_portfolio_scheduler.application.command.RegisterCustomerSubscriber.RegisterCustomerSubscriberResponse;
import com.matheus.srv_portfolio_scheduler.application.ports.input.queries.GetCustomerPortfolioUseCase;
import com.matheus.srv_portfolio_scheduler.application.ports.input.commands.RegisterCustomerSubscriberUseCase;
import com.matheus.srv_portfolio_scheduler.application.queries.GetCustomerPortfolio.GetCustomerPortfolioQuery;
import com.matheus.srv_portfolio_scheduler.application.queries.GetCustomerPortfolio.GetCustomerPortfolioResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(value = "v1/api/customers")
public class CustomersController implements SwaggerCustomersController {

    private final GetCustomerPortfolioUseCase getCustomerPortfolioUseCase;
    private final RegisterCustomerSubscriberUseCase registerCustomerSubscriberUseCase;

    @Override
    @PostMapping("adesao")
    public ResponseEntity<RegisterCustomerSubscriberResponse> registerCustomerSubscriber(
            @RequestBody @Valid RegisterCustomerSubscriberCommand command) {
        return ResponseEntity.status(201).body(registerCustomerSubscriberUseCase.handler(command));
    }

    @GetMapping("{id}/carteira")
    public ResponseEntity<GetCustomerPortfolioResponse> getCustomerPortfolio(@PathVariable long id) {

        return ResponseEntity.ok(getCustomerPortfolioUseCase.handler(new GetCustomerPortfolioQuery(id)));
    }
}
