package com.qa.qa_orchestrator_service.controller;

import com.qa.qa_orchestrator_service.repository.AnalysisRecord;
import com.qa.qa_orchestrator_service.service.AnalysisRecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * HistoryController
 *
 * Exposes analysis history and intelligence summaries from PostgreSQL.
 */
@RestController
@RequestMapping("/qa")
public class HistoryController {

    private final AnalysisRecordService analysisRecordService;

    public HistoryController(AnalysisRecordService analysisRecordService) {
        this.analysisRecordService = analysisRecordService;
    }

    // Last 10 analyses
    @GetMapping("/api/v1/history")
    public ResponseEntity<List<AnalysisRecord>> getRecentHistory() {
        return ResponseEntity.ok(analysisRecordService.getRecentAnalyses());
    }

    // History for a specific ticket
    @GetMapping("/api/v1/history/{issueKey}")
    public ResponseEntity<List<AnalysisRecord>> getHistoryForIssue(@PathVariable String issueKey) {
        return ResponseEntity.ok(analysisRecordService.getHistoryForIssue(issueKey.toUpperCase()));
    }

    // High risk tickets
    @GetMapping("/api/v1/intelligence/high-risk")
    public ResponseEntity<List<AnalysisRecord>> getHighRiskTickets() {
        return ResponseEntity.ok(analysisRecordService.getHighRiskTickets());
    }

    // Blocked tickets
    @GetMapping("/api/v1/intelligence/blocked")
    public ResponseEntity<List<AnalysisRecord>> getBlockedTickets() {
        return ResponseEntity.ok(analysisRecordService.getBlockedTickets());
    }

    // Overall intelligence summary
    @GetMapping("/api/v1/intelligence/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        return ResponseEntity.ok(analysisRecordService.getSummary());
    }
}