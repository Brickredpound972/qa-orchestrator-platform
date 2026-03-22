package com.qa.qa_orchestrator_service.controller;

import com.qa.qa_orchestrator_service.repository.AnalysisRecord;
import com.qa.qa_orchestrator_service.service.AnalysisRecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * HistoryController
 *
 * Exposes analysis history from PostgreSQL.
 */
@RestController
@RequestMapping("/qa")
public class HistoryController {

    private final AnalysisRecordService analysisRecordService;

    public HistoryController(AnalysisRecordService analysisRecordService) {
        this.analysisRecordService = analysisRecordService;
    }

    @GetMapping("/api/v1/history")
    public ResponseEntity<List<AnalysisRecord>> getRecentHistory() {
        return ResponseEntity.ok(analysisRecordService.getRecentAnalyses());
    }

    @GetMapping("/api/v1/history/{issueKey}")
    public ResponseEntity<List<AnalysisRecord>> getHistoryForIssue(@PathVariable String issueKey) {
        return ResponseEntity.ok(analysisRecordService.getHistoryForIssue(issueKey.toUpperCase()));
    }
}