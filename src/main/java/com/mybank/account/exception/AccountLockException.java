package com.mybank.account.exception;

public class AccountLockException  extends Exception{

	private static final long serialVersionUID = -5790771680755146612L;
	private String message;
	
	public enum AccountLockError{

        ACCOUNT_ALREADY_LOCKED("Account is already locked. Can't perform the transaction");

        final String message;
        AccountLockError(String message){
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

	public AccountLockException(String message){
        this.setMessage(message);
    }

    public AccountLockException(AccountLockError error){
        this.setMessage(error.getMessage());
    }

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}