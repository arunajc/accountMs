package com.mybank.account.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.mybank.account.exception.AccountLockException;
import com.mybank.account.exception.AccountTransactionException;
import com.mybank.account.exception.GeneralException;
import com.mybank.account.exception.ValidationException;
import com.mybank.account.model.AccountDetails;
import com.mybank.account.model.TransactionDetails;
import com.mybank.account.service.AccountService;

@RestController
public class AccountApiImpl implements AccountApi{
	
	@Autowired
	AccountService accountService;
	
	@Override
	public ResponseEntity<AccountDetails> createAccount(@Valid AccountDetails accountDetails) 
			throws GeneralException {
		AccountDetails accountDetailsResponse = accountService.createAccount(accountDetails);
		return new ResponseEntity<>(accountDetailsResponse, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<TransactionDetails> doTransaction(@Valid TransactionDetails transactionDetails)
			throws AccountLockException, AccountTransactionException, GeneralException, ValidationException {
		TransactionDetails transactionDetailsResponse = accountService.doTransaction(transactionDetails);
		return new ResponseEntity<>(transactionDetailsResponse, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<AccountDetails> checkBalance(long accountId) throws ValidationException, GeneralException {
		AccountDetails accountDetailsResponse = accountService.checkBalance(accountId);
		return new ResponseEntity<>(accountDetailsResponse, HttpStatus.OK);
	}
}
