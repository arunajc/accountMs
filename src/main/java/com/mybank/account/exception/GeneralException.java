package com.mybank.account.exception;

public class GeneralException extends Exception{

    private static final long serialVersionUID = 5929655506222077586L;
    private String message;

    public enum GeneralError{

        UNEXPECTED_ERROR("Unexpected error occured");

        final String message;
        
        GeneralError(String message){
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    public GeneralException(String message){
        this.message = message;
    }

    public GeneralException(GeneralError generalError){
        this.message = generalError.getMessage();
    }

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
    
    

}
