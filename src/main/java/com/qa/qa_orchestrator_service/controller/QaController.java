package com.qa.qa_orchestrator_service.controller;

import com.qa.qa_orchestrator_service.service.QaOrchestratorService;
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

    @PostMapping(value = "/run/{issueKey}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> run(@PathVariable String issueKey) {
        return Map.of("output", service.runAnalysis(issueKey));
    }
}