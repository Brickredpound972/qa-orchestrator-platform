package com.qa.qa_orchestrator_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

/**
 * GlobalExceptionHandler
 *
 * Converts unhandled exceptions into structured JSON error responses.
 *
 * Before: Spring default error page (HTML or ugly JSON)
 * After:  Clean, consistent error contract
 *
 * Example response:
 * {
 *   "status": 500,
 *   "error": "Internal Server Error",
 *   "message": "Jira issue not found: PROJ-99",
 *   "timestamp": "2026-03-21T18:00:00Z"
 * }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException e) {
        return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e) {
        String message = e.getMessage();

        // Jira 404
        if (message != null && message.contains("404")) {
            return buildError(HttpStatus.NOT_FOUND, "Jira issue not found. Check the issue key.");
        }

        // Jira auth
        if (message != null && message.contains("401")) {
            return buildError(HttpStatus.UNAUTHORIZED, "Jira authentication failed. Check credentials.");
        }

        // Groq rate limit
        if (message != null && message.contains("429")) {
            return buildError(HttpStatus.TOO_MANY_REQUESTS,
                    "LLM rate limit reached. Please try again in a few minutes.");
        }

        return buildError(HttpStatus.INTERNAL_SERVER_ERROR,
                "Analysis failed. Please try again.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again.");
    }

    private ResponseEntity<Map<String, Object>> buildError(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message != null ? message : "Unknown error",
                "timestamp", Instant.now().toString()
        ));
    }
}