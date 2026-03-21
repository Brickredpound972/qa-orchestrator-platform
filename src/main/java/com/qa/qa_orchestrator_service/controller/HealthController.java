package com.qa.qa_orchestrator_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * HealthController
 *
 * Simple health check endpoint.
 * Used by Render, Copilot Studio, and monitoring tools
 * to verify the service is alive.
 */
@RestController
public class HealthController {

    private static final String VERSION = "v2";
    private static final Instant START_TIME = Instant.now();

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "qa-orchestrator-service",
                "version", VERSION,
                "timestamp", Instant.now().toString(),
                "uptime", getUptime()
        ));
    }

    private String getUptime() {
        long seconds = Instant.now().getEpochSecond() - START_TIME.getEpochSecond();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%dh %dm %ds", hours, minutes, secs);
    }
}