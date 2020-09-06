package com.mybank.account.controller;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mybank.account.exception.AccountLockException;
import com.mybank.account.exception.AccountTransactionException;
import com.mybank.account.exception.GeneralException;
import com.mybank.account.exception.ValidationException;
import com.mybank.account.model.AccountDetails;
import com.mybank.account.model.TransactionDetails;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "account API")
@RequestMapping("/account")
public interface AccountApi {
	
	@ApiOperation(value="create a new account", nickname = "createAccount", tags = {"accountMs"})
	@ApiResponses(value = {
			@ApiResponse(code=405, message = "Invalid Input"),
			@ApiResponse(code=500, message = "Internal Server Error"),
			@ApiResponse(code=200, message = "Success")
	})
	@PostMapping(value = "/create", produces = "application/json", consumes = "application/json")
	ResponseEntity<AccountDetails> createAccount(
			@Valid @RequestBody AccountDetails accountDetails) 
					throws GeneralException;

	@ApiOperation(value="Post a transaction", nickname = "transact", tags = {"accountMs"})
	@ApiResponses(value = {
			@ApiResponse(code=409, message = "Could not get lock on the account"),
			@ApiResponse(code=409, message = "No account found with given details"),
			@ApiResponse(code=409, message = "Insufficient balance in account"),
			@ApiResponse(code=405, message = "Invalid input"),
			@ApiResponse(code=500, message = "Internal Server Error"),
			@ApiResponse(code=200, message = "Success")
	})
	@PostMapping(value = "/transact", produces = "application/json", consumes = "application/json")
	ResponseEntity<TransactionDetails> doTransaction(
			@Valid @RequestBody TransactionDetails transactionDetails
			) throws AccountLockException, AccountTransactionException, GeneralException, ValidationException;

	@ApiOperation(value="Check account balance", nickname = "balance", tags = {"accountMs"})
	@ApiResponses(value = {
			@ApiResponse(code=405, message = "Invalid input"),
			@ApiResponse(code=409, message = "No account found with given details"),
			@ApiResponse(code=500, message = "Internal Server Error"),
			@ApiResponse(code=200, message = "Success")
	})
	@GetMapping(value = "/balance", produces = "application/json")
	ResponseEntity<AccountDetails> checkBalance(
			@RequestParam(value = "accountId") long accountId
			) throws ValidationException, GeneralException, AccountTransactionException;

}
