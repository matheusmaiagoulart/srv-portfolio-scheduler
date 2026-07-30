package com.matheus.srv_portfolio_scheduler.application.ports.input.commands;

import com.matheus.srv_portfolio_scheduler.application.command.UpdateMonthlyAmount.UpdateMonthlyAmountCommand;
import com.matheus.srv_portfolio_scheduler.application.command.UpdateMonthlyAmount.UpdateMonthlyAmountResponse;

public interface UpdateMonthlyAmountUseCase {

    UpdateMonthlyAmountResponse handler(long customerId, UpdateMonthlyAmountCommand command);
}


