package com.example.springai.exception;

import com.example.springai.dto.ApiErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global REST exception handler enforcing enterprise production error hygiene.
 * Intercepts validation errors and unexpected exceptions to prevent stack trace disclosure (CWE-209 / OWASP).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles Jakarta Bean Validation errors on @Valid request bodies.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        log.warn("Validation failed for request: {}", errors);

        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                "Input payload validation failed. Check fieldErrors for details.",
                errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles illegal argument exceptions (e.g. resource not found or invalid input).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument error: {}", ex.getMessage());
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Fallback general exception handler masking internal details in production.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex) {
        log.error("Unhandled exception intercepted: {}", ex.getMessage(), ex);

        // Check if the root cause is a Gemini authentication error
        Throwable root = ex;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        String rootMsg = root.getMessage() != null ? root.getMessage() : ex.getMessage();
        if (rootMsg != null && (rootMsg.contains("API_KEY_INVALID") || rootMsg.contains("401") || rootMsg.contains("your_gemini_api_key_here"))) {
            ApiErrorResponse authError = new ApiErrorResponse(
                    HttpStatus.UNAUTHORIZED.value(),
                    "AI Authentication Error",
                    "Gemini API authentication failed. Configure a valid Google Gemini API Key via 'SPRING_AI_GOOGLE_GENAI_API_KEY'."
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(authError);
        }

        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred while processing the request. Contact system administrator."
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
