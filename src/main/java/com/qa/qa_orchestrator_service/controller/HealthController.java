package com.qa.qa_orchestrator_service.controller;

import com.qa.qa_orchestrator_service.service.AnalysisRecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * HealthController
 *
 * Health check endpoint with intelligence summary.
 */
@RestController
@RequestMapping("/qa")
public class HealthController {

    private static final String VERSION = "v2";
    private static final Instant START_TIME = Instant.now();

    private final AnalysisRecordService analysisRecordService;

    public HealthController(AnalysisRecordService analysisRecordService) {
        this.analysisRecordService = analysisRecordService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "qa-orchestrator-service");
        response.put("version", VERSION);
        response.put("timestamp", Instant.now().toString());
        response.put("uptime", getUptime());

        try {
            Map<String, Object> summary = analysisRecordService.getSummary();
            response.put("intelligence", summary);
        } catch (Exception e) {
            response.put("intelligence", "unavailable");
        }

        return ResponseEntity.ok(response);
    }

    private String getUptime() {
        long seconds = Instant.now().getEpochSecond() - START_TIME.getEpochSecond();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%dh %dm %ds", hours, minutes, secs);
    }
}