package com.example.springai.dto;

import java.time.Instant;
import java.util.Map;

/**
 * Standard structured API error response adhering to enterprise security guidelines.
 * Prevents stack trace leakage and internal system detail exposure.
 */
public record ApiErrorResponse(
        int status,
        String error,
        String message,
        Map<String, String> fieldErrors,
        Instant timestamp
) {
    public ApiErrorResponse(int status, String error, String message) {
        this(status, error, message, null, Instant.now());
    }

    public ApiErrorResponse(int status, String error, String message, Map<String, String> fieldErrors) {
        this(status, error, message, fieldErrors, Instant.now());
    }
}
