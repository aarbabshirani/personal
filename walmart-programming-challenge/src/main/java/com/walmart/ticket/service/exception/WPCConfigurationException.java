package com.walmart.ticket.service.exception;

public class WPCConfigurationException extends RuntimeException{

	private static final long serialVersionUID = 2530196218579850592L;

	public WPCConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public WPCConfigurationException(String message) {
        super(message);
    }

    public WPCConfigurationException(Throwable cause) {
        super(cause);
    }

}
