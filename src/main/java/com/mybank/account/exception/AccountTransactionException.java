package com.mybank.account.exception;

public class AccountTransactionException extends Exception{

    private static final long serialVersionUID = -6528138917073327274L;
    private String message;

    public enum AccountTransactionError{

        ACCOUNT_NOT_FOUND("No account found with given details"),
        INSUFFICIENT_FUNDS("Insufficient balance in account");

        final String message;
        AccountTransactionError(String message){
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    public AccountTransactionException(String message){
        this.setMessage(message);
    }

    public AccountTransactionException(AccountTransactionException.AccountTransactionError error){
        this.setMessage(error.getMessage());
    }

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
