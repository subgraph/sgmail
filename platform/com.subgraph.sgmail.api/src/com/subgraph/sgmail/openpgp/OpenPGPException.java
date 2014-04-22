package com.subgraph.sgmail.openpgp;

public class OpenPGPException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public OpenPGPException(String message, Throwable cause) {
		super(message, cause);
	}

	public OpenPGPException(String message) {
		super(message);
	}

	public OpenPGPException(Throwable cause) {
		super(cause);
	}
}
