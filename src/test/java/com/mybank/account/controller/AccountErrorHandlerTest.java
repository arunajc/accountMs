package com.mybank.account.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybank.account.exception.AccountLockException;
import com.mybank.account.exception.AccountTransactionException;
import com.mybank.account.exception.GeneralException;
import com.mybank.account.exception.ValidationException;
import com.mybank.account.model.AccountDetails;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import com.mybank.account.model.Error;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

public class AccountErrorHandlerTest {

    AccountErrorHandler accountErrorHandler = new AccountErrorHandler();
    ObjectMapper objectMapper;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        accountErrorHandler.objectMapper = objectMapper;
    }

    @Test
    public void handleAccountTransactionException_ACCOUNT_NOT_FOUND() {

        AccountTransactionException accountTransactionException = new AccountTransactionException(AccountTransactionException.AccountTransactionError.ACCOUNT_NOT_FOUND);
        Object response = accountErrorHandler.handleAccountTransactionException(accountTransactionException);

        assertThat(response, instanceOf(ResponseEntity.class));
        assertEquals(HttpStatus.CONFLICT, ((ResponseEntity) response).getStatusCode());
        assertThat(((ResponseEntity) response).getBody(), instanceOf(Error.class));
        Error error = (Error) ((ResponseEntity) response).getBody();
        assertEquals("TRANSACTION-001", error.getCode());
        assertEquals("No account found with given details", error.getDescription());
    }

    @Test
    public void handleAccountTransactionException_INSUFFICIENT_FUNDS() {

        AccountTransactionException accountTransactionException = new AccountTransactionException(AccountTransactionException.AccountTransactionError.INSUFFICIENT_FUNDS);
        Object response = accountErrorHandler.handleAccountTransactionException(accountTransactionException);

        assertThat(response, instanceOf(ResponseEntity.class));
        assertEquals(HttpStatus.CONFLICT, ((ResponseEntity) response).getStatusCode());
        assertThat(((ResponseEntity) response).getBody(), instanceOf(Error.class));
        Error error = (Error) ((ResponseEntity) response).getBody();
        assertEquals("TRANSACTION-001", error.getCode());
        assertEquals("Insufficient balance in account", error.getDescription());
    }

    @Test
    public void handleAccountLockException_ACCOUNT_ALREADY_LOCKED() {

        AccountLockException accountTransactionException = new AccountLockException(AccountLockException.AccountLockError.ACCOUNT_ALREADY_LOCKED);
        Object response = accountErrorHandler.handleAccountLockException(accountTransactionException);

        assertThat(response, instanceOf(ResponseEntity.class));
        assertEquals(HttpStatus.CONFLICT, ((ResponseEntity) response).getStatusCode());
        assertThat(((ResponseEntity) response).getBody(), instanceOf(Error.class));
        Error error = (Error) ((ResponseEntity) response).getBody();
        assertEquals("TRANSACTION-002", error.getCode());
        assertEquals("Account is already locked. Can't perform the transaction", error.getDescription());
    }

    @Test
    public void handleGeneralException() {

        GeneralException generalException = new GeneralException(GeneralException.GeneralError.UNEXPECTED_ERROR);
        Object response = accountErrorHandler.handleGeneralException(generalException);

        assertThat(response, instanceOf(ResponseEntity.class));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ((ResponseEntity) response).getStatusCode());
        assertThat(((ResponseEntity) response).getBody(), instanceOf(Error.class));
        Error error = (Error) ((ResponseEntity) response).getBody();
        assertEquals("GENERAL-001", error.getCode());
        assertEquals("Unexpected error occured", error.getDescription());
    }

    @Test
    public void handleValidationException_INVALID_ACCOUNT_ID() {

        ValidationException validationException = new ValidationException(ValidationException.ValidationError.INVALID_ACCOUNT_ID);
        Object response = accountErrorHandler.handleValidationException(validationException);

        assertThat(response, instanceOf(ResponseEntity.class));
        assertEquals(HttpStatus.BAD_REQUEST, ((ResponseEntity) response).getStatusCode());
        assertThat(((ResponseEntity) response).getBody(), instanceOf(Error.class));
        Error error = (Error) ((ResponseEntity) response).getBody();
        assertEquals("VALIDATION-001", error.getCode());
        assertEquals("Invalid input - Invalid accountId", error.getDescription());
    }

    @Test
    public void handleValidationException_IINVALID_REQUEST() {

        ValidationException validationException = new ValidationException(ValidationException.ValidationError.INVALID_REQUEST);
        Object response = accountErrorHandler.handleValidationException(validationException);

        assertThat(response, instanceOf(ResponseEntity.class));
        assertEquals(HttpStatus.BAD_REQUEST, ((ResponseEntity) response).getStatusCode());
        assertThat(((ResponseEntity) response).getBody(), instanceOf(Error.class));
        Error error = (Error) ((ResponseEntity) response).getBody();
        assertEquals("VALIDATION-001", error.getCode());
        assertEquals("Invalid input", error.getDescription());
    }

    @Test
    public void handleMethodArgumentExceptions_many() throws NoSuchMethodException {

        AccountDetails accountDetails = new AccountDetails();

        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(accountDetails, "accountDetails");
        errors.rejectValue("userName", "invalid", "invalid username");
        MethodParameter parameter = new MethodParameter(AccountDetails.class.getMethod("getUserName"), -1);
        MethodArgumentNotValidException methodArgumentNotValidException = new MethodArgumentNotValidException(parameter, errors);

        Object response = accountErrorHandler.handleMethodArgumentExceptions(methodArgumentNotValidException);

        assertThat(response, instanceOf(ResponseEntity.class));
        assertEquals(HttpStatus.BAD_REQUEST, ((ResponseEntity) response).getStatusCode());
        assertThat(((ResponseEntity) response).getBody(), instanceOf(Error.class));
        Error error = (Error) ((ResponseEntity) response).getBody();
        assertEquals("VALIDATION-002", error.getCode());
        assertEquals("{\"userName\":\"invalid username\"}", error.getDescription());
    }

}