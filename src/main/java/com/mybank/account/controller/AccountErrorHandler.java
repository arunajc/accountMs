package com.mybank.account.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybank.account.exception.AccountLockException;
import com.mybank.account.exception.AccountTransactionException;
import com.mybank.account.exception.GeneralException;
import com.mybank.account.exception.ValidationException;
import com.mybank.account.model.Error;

@ControllerAdvice
public class AccountErrorHandler {

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
}
