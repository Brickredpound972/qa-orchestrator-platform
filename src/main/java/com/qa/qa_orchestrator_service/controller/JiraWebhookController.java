package com.qa.qa_orchestrator_service.controller;

import com.qa.qa_orchestrator_service.model.QaAnalysisResult;
import com.qa.qa_orchestrator_service.service.QaOrchestratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * JiraWebhookController
 *
 * Receives Jira webhook events and triggers QA analysis
 * automatically when a ticket moves to "In Progress".
 */
@RestController
@RequestMapping("/qa/webhook")
public class JiraWebhookController {

    private static final Logger log = LoggerFactory.getLogger(JiraWebhookController.class);
    private static final String TRIGGER_STATUS = "In Progress";

    private final QaOrchestratorService orchestratorService;

    public JiraWebhookController(QaOrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    @PostMapping("/jira")
    public ResponseEntity<Map<String, Object>> handleJiraWebhook(
            @RequestBody Map<String, Object> payload) {

        try {
            String issueKey = extractIssueKey(payload);
            String toStatus = extractToStatus(payload);
            String event = extractEvent(payload);

            log.info("[WEBHOOK] Received event={} | issue={} | status={}",
                    event, issueKey, toStatus);

            if (issueKey == null) {
                log.warn("[WEBHOOK] No issue key found in payload, skipping");
                return ResponseEntity.ok(Map.of("status", "skipped", "reason", "no issue key"));
            }

            if (!TRIGGER_STATUS.equalsIgnoreCase(toStatus)) {
                log.info("[WEBHOOK] Status '{}' is not trigger status '{}', skipping",
                        toStatus, TRIGGER_STATUS);
                return ResponseEntity.ok(Map.of(
                        "status", "skipped",
                        "reason", "status is not In Progress",
                        "issueKey", issueKey,
                        "toStatus", toStatus != null ? toStatus : "unknown"));
            }

            log.info("[WEBHOOK] Triggering analysis for {} (status: {})", issueKey, toStatus);

            QaAnalysisResult result = orchestratorService.runAnalysis(issueKey);

            log.info("[WEBHOOK] Analysis complete for {} | risk={} | release={}",
                    issueKey, result.getRiskLevel(), result.getReleaseRecommendation());

            return ResponseEntity.ok(Map.of(
                    "status", "analyzed",
                    "issueKey", issueKey,
                    "riskLevel", result.getRiskLevel() != null ? result.getRiskLevel() : "UNKNOWN",
                    "riskScore", result.getRiskScore() != null ? result.getRiskScore() : 0,
                    "releaseRecommendation", result.getReleaseRecommendation() != null ? result.getReleaseRecommendation() : "UNKNOWN"
            ));

        } catch (Exception e) {
            log.error("[WEBHOOK] Error processing webhook: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                    "status", "error",
                    "reason", e.getMessage()
            ));
        }
    }

    private String extractIssueKey(Map<String, Object> payload) {
        try {
            Object issue = payload.get("issue");
            if (issue instanceof Map) {
                Object key = ((Map<?, ?>) issue).get("key");
                if (key != null) return key.toString();
            }
        } catch (Exception e) {
            log.warn("[WEBHOOK] Could not extract issue key: {}", e.getMessage());
        }
        return null;
    }

    private String extractToStatus(Map<String, Object> payload) {
        try {
            Object changelog = payload.get("changelog");
            if (changelog instanceof Map) {
                Object items = ((Map<?, ?>) changelog).get("items");
                if (items instanceof java.util.List) {
                    for (Object item : (java.util.List<?>) items) {
                        if (item instanceof Map) {
                            Map<?, ?> itemMap = (Map<?, ?>) item;
                            Object field = itemMap.get("field");
                            if ("status".equals(field)) {
                                Object toString = itemMap.get("toString");
                                if (toString != null) return toString.toString();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[WEBHOOK] Could not extract status: {}", e.getMessage());
        }
        return null;
    }

    private String extractEvent(Map<String, Object> payload) {
        Object event = payload.get("webhookEvent");
        return event != null ? event.toString() : "unknown";
    }
}