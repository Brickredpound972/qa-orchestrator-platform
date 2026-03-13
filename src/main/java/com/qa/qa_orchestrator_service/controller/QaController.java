package com.qa.qa_orchestrator_service.controller;

import com.qa.qa_orchestrator_service.model.QaAnalyzeRequest;
import com.qa.qa_orchestrator_service.model.QaAnalyzeResponse;
import com.qa.qa_orchestrator_service.service.QaOrchestratorService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/qa")
public class QaController {

    private final QaOrchestratorService service;

    public QaController(QaOrchestratorService service) {
        this.service = service;
    }

    /**
     * Canonical API endpoint (primary endpoint going forward)
     */
    @PostMapping(
            value = "/api/v1/qa/analyze",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public QaAnalyzeResponse analyze(@RequestBody QaAnalyzeRequest request) {
        return executeAnalysis(request.getIssueKey());
    }

    /**
     * Legacy endpoint used by earlier integrations (Copilot fallback)
     */
    @PostMapping(
            value = "/run/{issueKey}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public QaAnalyzeResponse run(@PathVariable String issueKey) {
        return executeAnalysis(issueKey);
    }

    /**
     * Compatibility alias for unstable clients or tool mappings
     */
    @PostMapping(
            value = "/analyze",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public QaAnalyzeResponse analyzeAlias(@RequestBody QaAnalyzeRequest request) {
        return executeAnalysis(request.getIssueKey());
    }

    /**
     * Centralized execution logic
     */
    private QaAnalyzeResponse executeAnalysis(String issueKey) {
        String result = service.runAnalysis(issueKey);
        return new QaAnalyzeResponse(result);
    }
}