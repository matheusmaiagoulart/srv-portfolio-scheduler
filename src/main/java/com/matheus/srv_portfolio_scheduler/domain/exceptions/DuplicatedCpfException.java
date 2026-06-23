package com.matheus.srv_portfolio_scheduler.domain.exceptions;

public class DuplicatedCpfException extends BusinessException {

    private static final String errorCode = "DUPLICATED_CPF";
    private static final String errorMessage = "CPF already exists: ";

    public DuplicatedCpfException(String cpf) {
        super(errorCode, errorMessage.concat(cpf));
    }
}
