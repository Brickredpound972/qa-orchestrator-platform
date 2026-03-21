package com.qa.qa_orchestrator_service.controller;

import com.qa.qa_orchestrator_service.model.QaAnalysisResult;
import com.qa.qa_orchestrator_service.model.QaAnalyzeRequest;
import com.qa.qa_orchestrator_service.model.QaAnalyzeResponse;
import com.qa.qa_orchestrator_service.service.QaOrchestratorService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * QaController
 *
 * Exposes three endpoints for backward compatibility.
 * All routes delegate to executeAnalysis() which runs the pipeline once.
 *
 * KEY FIX:
 * Previously the controller called runAnalysis() + buildStructuredAnalysis() separately,
 * which ran the pipeline twice and made two Jira API calls.
 * Now it calls runAnalysis() once and wraps the result.
 */
@RestController
@RequestMapping("/qa")
public class QaController {

    private final QaOrchestratorService service;

    public QaController(QaOrchestratorService service) {
        this.service = service;
    }

    /**
     * Canonical endpoint — primary going forward
     */
    @PostMapping(
            value = "/api/v1/qa/analyze",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public QaAnalyzeResponse analyze(@RequestBody QaAnalyzeRequest request) {
        return executeAnalysis(request.getIssueKey());
    }

    /**
     * Legacy endpoint — kept for Copilot Studio compatibility
     */
    @PostMapping(value = "/run/{issueKey}", produces = MediaType.APPLICATION_JSON_VALUE)
    public QaAnalyzeResponse run(@PathVariable String issueKey) {
        return executeAnalysis(issueKey);
    }

    /**
     * Alias endpoint — kept for older integrations
     */
    @PostMapping(
            value = "/analyze",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public QaAnalyzeResponse analyzeAlias(@RequestBody QaAnalyzeRequest request) {
        return executeAnalysis(request.getIssueKey());
    }

    /**
     * Single execution point — pipeline runs exactly once per request.
     */
    private QaAnalyzeResponse executeAnalysis(String issueKey) {
        QaAnalysisResult result = service.runAnalysis(issueKey);
        return new QaAnalyzeResponse(result.getAnalysisSummary(), result);
    }
}