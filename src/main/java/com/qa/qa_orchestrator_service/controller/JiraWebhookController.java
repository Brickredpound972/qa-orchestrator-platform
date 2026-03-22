package com.qa.qa_orchestrator_service.controller;

import com.qa.qa_orchestrator_service.model.QaAnalysisResult;
import com.qa.qa_orchestrator_service.repository.AnalysisRecord;
import com.qa.qa_orchestrator_service.service.AnalysisRecordService;
import com.qa.qa_orchestrator_service.service.QaOrchestratorService;
import com.qa.qa_orchestrator_service.service.stage.ReleaseSummaryStage;
import com.qa.qa_orchestrator_service.jira.JiraClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * JiraWebhookController
 *
 * Handles Jira webhook events:
 * - "In Progress" → triggers full QA analysis pipeline
 * - "Done"        → generates QA release summary from history
 */
@RestController
@RequestMapping("/qa/webhook")
public class JiraWebhookController {

    private static final Logger log = LoggerFactory.getLogger(JiraWebhookController.class);
    private static final String TRIGGER_STATUS = "In Progress";
    private static final String DONE_STATUS = "Done";

    private final QaOrchestratorService orchestratorService;
    private final AnalysisRecordService analysisRecordService;
    private final ReleaseSummaryStage releaseSummaryStage;
    private final JiraClient jiraClient;

    public JiraWebhookController(
            QaOrchestratorService orchestratorService,
            AnalysisRecordService analysisRecordService,
            ReleaseSummaryStage releaseSummaryStage,
            JiraClient jiraClient) {
        this.orchestratorService = orchestratorService;
        this.analysisRecordService = analysisRecordService;
        this.releaseSummaryStage = releaseSummaryStage;
        this.jiraClient = jiraClient;
    }

    @PostMapping("/jira")
    public ResponseEntity<Map<String, Object>> handleJiraWebhook(
            @RequestBody Map<String, Object> payload) {
        try {
            String issueKey = extractIssueKey(payload);
            String toStatus = extractToStatus(payload);
            String event = extractEvent(payload);

            log.info("[WEBHOOK] Received event={} | issue={} | status={}", event, issueKey, toStatus);

            if (issueKey == null) {
                log.warn("[WEBHOOK] No issue key found, skipping");
                return ResponseEntity.ok(Map.of("status", "skipped", "reason", "no issue key"));
            }

            // In Progress → full analysis pipeline
            if (TRIGGER_STATUS.equalsIgnoreCase(toStatus)) {
                return handleInProgress(issueKey);
            }

            // Done → release summary
            if (DONE_STATUS.equalsIgnoreCase(toStatus)) {
                return handleDone(issueKey);
            }

            log.info("[WEBHOOK] Status '{}' not handled, skipping", toStatus);
            return ResponseEntity.ok(Map.of(
                    "status", "skipped",
                    "reason", "status not handled",
                    "toStatus", toStatus != null ? toStatus : "unknown"));

        } catch (Exception e) {
            log.error("[WEBHOOK] Error processing webhook: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("status", "error", "reason", e.getMessage()));
        }
    }

    private ResponseEntity<Map<String, Object>> handleInProgress(String issueKey) {
        log.info("[WEBHOOK] Triggering analysis for {} (status: In Progress)", issueKey);
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
    }

    private ResponseEntity<Map<String, Object>> handleDone(String issueKey) {
        log.info("[WEBHOOK] Generating release summary for {} (status: Done)", issueKey);

        List<AnalysisRecord> history = analysisRecordService.getHistoryForIssue(issueKey);

        if (history.isEmpty()) {
            log.warn("[WEBHOOK] No analysis history found for {}, skipping release summary", issueKey);
            return ResponseEntity.ok(Map.of(
                    "status", "skipped",
                    "reason", "no analysis history found",
                    "issueKey", issueKey));
        }

        String summary = releaseSummaryStage.generateSummary(issueKey, history);
        analysisRecordService.markAsCompleted(issueKey, summary);
        jiraClient.addComment(issueKey, "QA Release Summary\n\n" + summary);

        log.info("[WEBHOOK] Release summary complete for {}", issueKey);
        return ResponseEntity.ok(Map.of(
                "status", "release_summary_generated",
                "issueKey", issueKey,
                "analysesReviewed", history.size()
        ));
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
                if (items instanceof List) {
                    for (Object item : (List<?>) items) {
                        if (item instanceof Map) {
                            Map<?, ?> itemMap = (Map<?, ?>) item;
                            if ("status".equals(itemMap.get("field"))) {
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