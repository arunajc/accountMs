package com.mybank.account.controller;

import com.mybank.account.exception.AccountLockException;
import com.mybank.account.exception.AccountTransactionException;
import com.mybank.account.exception.GeneralException;
import com.mybank.account.exception.ValidationException;
import com.mybank.account.model.AccountDetails;
import com.mybank.account.model.TransactionDetails;
import com.mybank.account.service.AccountService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class AccountApiImplTest {

    @InjectMocks
    AccountApiImpl accountApi;

    @Mock
    AccountService accountService;

    @Test
    public void createAccount_success() throws GeneralException {
        AccountDetails accountDetails = new AccountDetails();
        accountDetails.setUserName("cust1");
        Mockito.when(accountService.createAccount(accountDetails)).thenReturn(accountDetails);

        Object response = accountApi.createAccount(accountDetails);

        assertThat(response, instanceOf(ResponseEntity.class));
        assertEquals(HttpStatus.OK, ((ResponseEntity)response).getStatusCode());
        assertThat(((ResponseEntity)response).getBody(), instanceOf(AccountDetails.class));
        AccountDetails accountDetailsResponse =  (AccountDetails) ((ResponseEntity)response).getBody();
        assertEquals("cust1", accountDetailsResponse.getUserName());
    }

    @Test(expected = GeneralException.class)
    public void createAccount_GeneralException() throws GeneralException {
        AccountDetails accountDetails = new AccountDetails();
        Mockito
                .doThrow(new GeneralException(GeneralException.GeneralError.UNEXPECTED_ERROR))
                .when(accountService).createAccount(accountDetails);

        accountApi.createAccount(accountDetails);
    }

    @Test
    public void doTransaction_success()
            throws GeneralException, AccountTransactionException, ValidationException, AccountLockException {
        TransactionDetails transactionDetails = new TransactionDetails();
        transactionDetails.setAccountId(123456);
        transactionDetails.setAmount(new BigDecimal(10.09));

        Mockito.when(accountService.doTransaction(transactionDetails)).thenReturn(transactionDetails);

        Object response = accountApi.doTransaction(transactionDetails);

        assertThat(response, instanceOf(ResponseEntity.class));
        assertEquals(HttpStatus.OK, ((ResponseEntity)response).getStatusCode());
        assertThat(((ResponseEntity)response).getBody(), instanceOf(TransactionDetails.class));
        TransactionDetails transactionDetailsResponse =
                (TransactionDetails) ((ResponseEntity)response).getBody();
        assertEquals(123456, transactionDetailsResponse.getAccountId());
        assertEquals(new BigDecimal(10.09), transactionDetailsResponse.getAmount());
    }

    @Test(expected = GeneralException.class)
    public void doTransaction_GeneralException()
            throws GeneralException, AccountTransactionException, ValidationException, AccountLockException {
        TransactionDetails transactionDetails = new TransactionDetails();
        transactionDetails.setAccountId(123456);
        transactionDetails.setAmount(new BigDecimal(10.09));

        Mockito
                .doThrow(new GeneralException(GeneralException.GeneralError.UNEXPECTED_ERROR))
                .when(accountService).doTransaction(transactionDetails);

        accountApi.doTransaction(transactionDetails);
    }

    @Test(expected = AccountTransactionException.class)
    public void doTransaction_AccountTransactionException()
            throws GeneralException, AccountTransactionException, ValidationException, AccountLockException {
        TransactionDetails transactionDetails = new TransactionDetails();
        transactionDetails.setAccountId(123456);
        transactionDetails.setAmount(new BigDecimal(10.09));

        Mockito
                .doThrow(new AccountTransactionException(AccountTransactionException.AccountTransactionError.ACCOUNT_NOT_FOUND))
                .when(accountService).doTransaction(transactionDetails);

        accountApi.doTransaction(transactionDetails);
    }

    @Test(expected = ValidationException.class)
    public void doTransaction_ValidationException()
            throws GeneralException, AccountTransactionException, ValidationException, AccountLockException {
        TransactionDetails transactionDetails = new TransactionDetails();
        transactionDetails.setAccountId(123456);
        transactionDetails.setAmount(new BigDecimal(10.09));

        Mockito
                .doThrow(new ValidationException(ValidationException.ValidationError.INVALID_ACCOUNT_ID))
                .when(accountService).doTransaction(transactionDetails);

        accountApi.doTransaction(transactionDetails);
    }

    @Test(expected = AccountLockException.class)
    public void doTransaction_AccountLockException()
            throws GeneralException, AccountTransactionException, ValidationException, AccountLockException {
        TransactionDetails transactionDetails = new TransactionDetails();
        transactionDetails.setAccountId(123456);
        transactionDetails.setAmount(new BigDecimal(10.09));

        Mockito
                .doThrow(new AccountLockException(AccountLockException.AccountLockError.ACCOUNT_ALREADY_LOCKED))
                .when(accountService).doTransaction(transactionDetails);

        accountApi.doTransaction(transactionDetails);
    }

    @Test
    public void checkBalance_success() throws ValidationException, GeneralException, AccountTransactionException {
        long accountId = 123456789;
        AccountDetails accountDetails = new AccountDetails();
        accountDetails.setAccountId(accountId);
        accountDetails.setUserName("cust1");
        accountDetails.setBalance(new BigDecimal(1000.25));

        Mockito.when(accountService.checkBalance(accountId)).thenReturn(accountDetails);

        Object response = accountApi.checkBalance(accountId);

        assertThat(response, instanceOf(ResponseEntity.class));
        assertEquals(HttpStatus.OK, ((ResponseEntity)response).getStatusCode());
        assertThat(((ResponseEntity)response).getBody(), instanceOf(AccountDetails.class));
        AccountDetails accountDetailsResponse =
                (AccountDetails) ((ResponseEntity)response).getBody();
        assertEquals(123456789, accountDetails.getAccountId());
        assertEquals(new BigDecimal(1000.25), accountDetails.getBalance());
    }

    @Test(expected = ValidationException.class)
    public void checkBalance_ValidationException() throws ValidationException, GeneralException, AccountTransactionException {
        AccountDetails accountDetails = new AccountDetails();
        accountDetails.setUserName("cust1");
        accountDetails.setBalance(new BigDecimal(1000.25));

        long accountId = 123456789;
        Mockito
                .doThrow(new ValidationException(ValidationException.ValidationError.INVALID_ACCOUNT_ID))
                .when(accountService).checkBalance(accountId);

        accountApi.checkBalance(accountId);
    }

    @Test(expected = GeneralException.class)
    public void checkBalance_GeneralException() throws ValidationException, GeneralException, AccountTransactionException {
        long accountId = 123456789;
        AccountDetails accountDetails = new AccountDetails();
        accountDetails.setAccountId(accountId);
        accountDetails.setUserName("cust1");
        accountDetails.setBalance(new BigDecimal(1000.25));

        Mockito
                .doThrow(new GeneralException(GeneralException.GeneralError.UNEXPECTED_ERROR))
                .when(accountService).checkBalance(accountId);

        accountApi.checkBalance(accountId);
    }

}