package com.matheus.srv_portfolio_scheduler.adapters.exception.handler;

import com.matheus.srv_portfolio_scheduler.domain.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ExceptionResponse> businessException(BusinessException ex) {
        var httpStatus = HttpStatus.BAD_REQUEST;

        return ResponseEntity
                .status(httpStatus)
                .body(new ExceptionResponse(httpStatus.value(), ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(DuplicatedCpfException.class)
    public ResponseEntity<ExceptionResponse> duplicatedCpfException(DuplicatedCpfException ex) {
        var httpStatus = HttpStatus.CONFLICT;

        return ResponseEntity
                .status(httpStatus)
                .body(new ExceptionResponse(httpStatus.value(), ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(DuplicatedEmailException.class)
    public ResponseEntity<ExceptionResponse> duplicatedEmailException(DuplicatedEmailException ex) {
        var httpStatus = HttpStatus.CONFLICT;

        return ResponseEntity
                .status(httpStatus)
                .body(new ExceptionResponse(httpStatus.value(), ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(CotahistNotFoundException.class)
    public ResponseEntity<ExceptionResponse> cotahistNotFoundException(CotahistNotFoundException ex) {
        var httpStatus = HttpStatus.NOT_FOUND;

        return ResponseEntity
                .status(httpStatus)
                .body(new ExceptionResponse(httpStatus.value(), ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(IllegalPurchaseDayException.class)
    public ResponseEntity<ExceptionResponse> illegalPurchaseDayException(IllegalPurchaseDayException ex) {
        var httpStatus = HttpStatus.BAD_REQUEST;

        return ResponseEntity
                .status(httpStatus)
                .body(new ExceptionResponse(httpStatus.value(), ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(MasterAccountNotFoundException.class)
    public ResponseEntity<ExceptionResponse> masterAccountNotFoundException(MasterAccountNotFoundException ex) {
        var httpStatus = HttpStatus.NOT_FOUND;

        return ResponseEntity
                .status(httpStatus)
                .body(new ExceptionResponse(httpStatus.value(), ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(QuoteNotFoundException.class)
    public ResponseEntity<ExceptionResponse> quoteNotFoundException(QuoteNotFoundException ex) {
        var httpStatus = HttpStatus.NOT_FOUND;

        return ResponseEntity
                .status(httpStatus)
                .body(new ExceptionResponse(httpStatus.value(), ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(ActivePortfolioNotFoundException.class)
    public ResponseEntity<ExceptionResponse> activePortfolioNotFoundException(ActivePortfolioNotFoundException ex) {
        var httpStatus = HttpStatus.NOT_FOUND;

        return ResponseEntity
                .status(httpStatus)
                .body(new ExceptionResponse(httpStatus.value(), ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(CustomerAlreadyInactiveException.class)
    public ResponseEntity<ExceptionResponse> customerAlreadyInactiveException(CustomerAlreadyInactiveException ex) {
        return ResponseEntity
                .status(422)
                .body(new ExceptionResponse(422, ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        var errors = new HashMap<String, String>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((org.springframework.validation.FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}
