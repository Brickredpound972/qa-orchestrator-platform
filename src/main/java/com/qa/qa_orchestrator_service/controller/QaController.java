package com.qa.qa_orchestrator_service.controller;

import com.qa.qa_orchestrator_service.model.QaAnalyzeRequest;
import com.qa.qa_orchestrator_service.model.QaAnalyzeResponse;
import com.qa.qa_orchestrator_service.model.QaAnalysisResult;
import com.qa.qa_orchestrator_service.service.QaOrchestratorService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * QaController
 *
 * REST controller for the QA analysis pipeline.
 * Validates input before passing to the service layer.
 */
@RestController
@RequestMapping("/qa")
public class QaController {

    private final QaOrchestratorService service;

    public QaController(QaOrchestratorService service) {
        this.service = service;
    }

    @PostMapping(value = "/api/v1/qa/analyze", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public QaAnalyzeResponse analyze(@Valid @RequestBody QaAnalyzeRequest request) {
        return executeAnalysis(request.getIssueKey());
    }

    @PostMapping(value = "/run/{issueKey}", produces = MediaType.APPLICATION_JSON_VALUE)
    public QaAnalyzeResponse run(@PathVariable String issueKey) {
        return executeAnalysis(issueKey.trim().toUpperCase());
    }

    @PostMapping(value = "/analyze", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public QaAnalyzeResponse analyzeAlias(@Valid @RequestBody QaAnalyzeRequest request) {
        return executeAnalysis(request.getIssueKey());
    }

    private QaAnalyzeResponse executeAnalysis(String issueKey) {
        QaAnalysisResult result = service.runAnalysis(issueKey);
        return new QaAnalyzeResponse(result.getRawOutput(), result);
    }
}