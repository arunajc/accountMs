package com.mybank.account.service;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import com.mybank.account.exception.AccountLockException;
import com.mybank.account.exception.AccountTransactionException;
import com.mybank.account.exception.GeneralException;
import com.mybank.account.exception.ValidationException;
import com.mybank.account.model.AccountDetails;
import com.mybank.account.model.TransactionDetails;

public interface AccountService {

	AccountDetails createAccount(AccountDetails accountDetails) throws GeneralException;

	@Retryable(
			value = {AccountLockException.class }, 
			maxAttempts = 3,
			backoff = @Backoff(delay = 1000))
	TransactionDetails doTransaction(TransactionDetails transactionDetails) 
			throws AccountLockException, AccountTransactionException, GeneralException, ValidationException;

	AccountDetails checkBalance(long accountId) throws ValidationException, GeneralException;
}
