package com.mybank.account.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybank.account.exception.AccountLockException;
import com.mybank.account.exception.AccountTransactionException;
import com.mybank.account.exception.GeneralException;
import com.mybank.account.exception.ValidationException;
import com.mybank.account.model.Error;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class AccountErrorHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountErrorHandler.class);

    @Autowired
    ObjectMapper objectMapper;

    @ExceptionHandler(AccountTransactionException.class)
    @ResponseBody
    public ResponseEntity<Error> handleAccountTransactionException(
            final AccountTransactionException accountTransactionException){
        Error error = new Error("TRANSACTION-001",
                accountTransactionException.getMessage());

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
    
    @ExceptionHandler(AccountLockException.class)
    @ResponseBody
    public ResponseEntity<Error> handleAccountLockException(
            final AccountLockException accountLockException){
        Error error = new Error("TRANSACTION-002",
        		accountLockException.getMessage());

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
    
    @ExceptionHandler(GeneralException.class)
    @ResponseBody
    public ResponseEntity<Error> handleGeneralException(
            final GeneralException generalException){
        Error error = new Error("GENERAL-001", generalException.getMessage());

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @ExceptionHandler(ValidationException.class)
    @ResponseBody
    public ResponseEntity<Error> handleValidationException(
            final ValidationException validationException){
        Error error = new Error("VALIDATION-001",
                validationException.getMessage());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<Error> handleMethodArgumentExceptions(
            MethodArgumentNotValidException ex) {
        LOGGER.warn("Input payload validation failed");
        Map<String, String> validationFailedFields = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationFailedFields.put(fieldName, errorMessage);
        });

        String description = "Failed to get validation failure message";
        try {
            description = objectMapper.writeValueAsString(validationFailedFields);
        } catch (JsonProcessingException e) {
            LOGGER.warn("Could not extract the validation failure reason- ", e);
        }
        LOGGER.warn("Input payload validation failed. Reason(s): {}", description);

        Error error = new Error("VALIDATION-002", description);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
