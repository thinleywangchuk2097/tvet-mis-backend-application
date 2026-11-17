package com.moesd.tvet.mis.backend.application.exception;

import java.io.Serial;

/** Thrown when an institute change request cannot be processed. */
public class InstituteChangeException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	public InstituteChangeException(String message, Throwable cause) {
		super(message, cause);
	}
}
