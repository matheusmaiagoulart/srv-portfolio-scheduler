package com.matheus.srv_portfolio_scheduler.adapters.input.controller;

import com.matheus.srv_portfolio_scheduler.adapters.input.swagger.SwaggerCustomersController;
import com.matheus.srv_portfolio_scheduler.application.command.RegisterCustomerSubscriber.RegisterCustomerSubscriberCommand;
import com.matheus.srv_portfolio_scheduler.application.command.RegisterCustomerSubscriber.RegisterCustomerSubscriberResponse;
import com.matheus.srv_portfolio_scheduler.application.ports.input.RegisterCustomerSubscriberUseCase;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping(value = "v1/api/customers")
public class CustomersController implements SwaggerCustomersController {

    private final RegisterCustomerSubscriberUseCase registerCustomerSubscriberUseCase;

    @Override
    @PostMapping
    public ResponseEntity<RegisterCustomerSubscriberResponse> registerCustomerSubscriber(
            @RequestBody @Valid RegisterCustomerSubscriberCommand command) {
        return ResponseEntity.ok(registerCustomerSubscriberUseCase.handler(command));
    }
}
