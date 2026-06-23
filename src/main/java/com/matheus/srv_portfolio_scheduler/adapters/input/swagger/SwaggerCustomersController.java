package com.matheus.srv_portfolio_scheduler.adapters.input.swagger;

import com.matheus.srv_portfolio_scheduler.application.command.RegisterCustomerSubscriber.RegisterCustomerSubscriberCommand;
import com.matheus.srv_portfolio_scheduler.application.command.RegisterCustomerSubscriber.RegisterCustomerSubscriberResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "CustomersController")
public interface SwaggerCustomersController {

    @Operation(summary = "Criação do usuário",
            description = "Criação do usuário inscrito para participar da compra automatizada de ações da corretora.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Ok")})
    ResponseEntity<RegisterCustomerSubscriberResponse> registerCustomerSubscriber(
            @RequestBody @Valid RegisterCustomerSubscriberCommand command);
}
