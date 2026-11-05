package com.knoir.spring_boot_template.exception;

import org.springframework.http.HttpStatus;

public class ApiError extends RuntimeException {

	private final int status;
	private final Object data;

	public ApiError(int status, String message) {
		this(status, message, null);
	}

	public ApiError(int status, String message, Object data) {
		super(message);
		this.status = status;
		this.data = data;
	}

	public ApiError(HttpStatus status, String message) {
		this(status.value(), message, null);
	}

	public ApiError(HttpStatus status, String message, Object data) {
		this(status.value(), message, data);
	}

	public int getStatus() {
		return status;
	}

	public Object getData() {
		return data;
	}
}
