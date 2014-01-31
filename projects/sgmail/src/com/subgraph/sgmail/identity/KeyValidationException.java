package com.subgraph.sgmail.identity;

public class KeyValidationException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public KeyValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	public KeyValidationException(String message) {
		super(message);
	}

	public KeyValidationException(Throwable cause) {
		super(cause);
	}
}
