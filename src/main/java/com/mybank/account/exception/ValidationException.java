package com.mybank.account.exception;

public class ValidationException extends Exception{

    private static final long serialVersionUID = -5466103685790084580L;
    private String message;

    public enum ValidationError{

        INVALID_REQUEST("Invalid input"),
        INVALID_ACCOUNT_ID("Invalid input - Invalid accountId");

        final String message;
        ValidationError(String message){
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    public ValidationException(String message){
        this.setMessage(message);
    }

    public ValidationException(ValidationError error){
        this.setMessage(error.getMessage());
    }

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
