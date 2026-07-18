package com.matheus.srv_portfolio_scheduler.application.ports.output.commands;

import com.matheus.srv_portfolio_scheduler.domain.entities.Custody;

import java.util.List;

public interface CustodyRepositoryPort {

    List<Custody> saveAll(List<Custody> custodies);
}
