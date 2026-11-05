package com.knoir.spring_boot_template.exception;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import com.knoir.spring_boot_template.enumeration.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@Value("${app.environment}")
	private String environmentName;

	@ExceptionHandler({ NoResourceFoundException.class, NoHandlerFoundException.class })
	public ResponseEntity<Void> handleNotFound() {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<Map<String, Object>> handlePayloadTooLarge(MaxUploadSizeExceededException error) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("error", "Payload Too Large");
		return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE).body(body);
	}

	@ExceptionHandler(ApiError.class)
	public ResponseEntity<Map<String, Object>> handleApiError(ApiError error) {
		String stackTrace = getStackTrace(error);
		log.error("[Error]: {}", stackTrace);

		boolean isLocalOrDev = Environment.fromString(environmentName).isLocalOrDev();

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("error", error.getMessage());
		if (error.getData() != null) {
			body.put("data", error.getData());
		}
		if (isLocalOrDev) {
			body.put("stack", stackTrace);
		}

		return ResponseEntity.status(error.getStatus()).body(body);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleGenericException(Exception error) {
		String stackTrace = getStackTrace(error);
		log.error("[Error]: {}", stackTrace);

		boolean isLocalOrDev = Environment.fromString(environmentName).isLocalOrDev();

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("error", "Internal Server Error");
		if (isLocalOrDev) {
			body.put("stack", stackTrace);
		}

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
	}

	private String getStackTrace(Throwable throwable) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		throwable.printStackTrace(pw);
		return sw.toString();
	}
}
