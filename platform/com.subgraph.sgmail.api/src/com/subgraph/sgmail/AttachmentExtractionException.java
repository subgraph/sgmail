package com.subgraph.sgmail;

public class AttachmentExtractionException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public AttachmentExtractionException(String message) {
        super(message);
    }

    public AttachmentExtractionException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
