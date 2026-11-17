package com.moesd.tvet.mis.backend.application.exception;

import java.io.Serial;

/** Thrown when a call to Bhutan NDI (token API or NATS) fails. */
public class NdiIntegrationException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	public NdiIntegrationException(String message) {
		super(message);
	}

	public NdiIntegrationException(String message, Throwable cause) {
		super(message, cause);
	}
}
