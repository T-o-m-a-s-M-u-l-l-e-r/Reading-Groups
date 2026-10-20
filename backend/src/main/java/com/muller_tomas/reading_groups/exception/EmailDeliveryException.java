package com.muller_tomas.reading_groups.exception;

public class EmailDeliveryException extends RuntimeException {
	public EmailDeliveryException(String message, Throwable cause) {
		super(message, cause);
	}
}
