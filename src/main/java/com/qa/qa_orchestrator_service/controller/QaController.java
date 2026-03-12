package com.qa.qa_orchestrator_service.controller;

import com.qa.qa_orchestrator_service.service.QaOrchestratorService;
import com.qa.qa_orchestrator_service.model.QaAnalyzeRequest;
import com.qa.qa_orchestrator_service.model.QaAnalyzeResponse;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/qa")
public class QaController {

    private final QaOrchestratorService service;

    public QaController(QaOrchestratorService service) {
        this.service = service;
    }

    // Existing endpoint (kept for backward compatibility)
    @PostMapping(value = "/run/{issueKey}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> run(@PathVariable String issueKey) {
        return Map.of("output", service.runAnalysis(issueKey));
    }

    // New v1 API endpoint
    @PostMapping(
            value = "/api/v1/qa/analyze",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public QaAnalyzeResponse analyze(@RequestBody QaAnalyzeRequest request) {
        String result = service.runAnalysis(request.getIssueKey());
        return new QaAnalyzeResponse(result);
    }
}