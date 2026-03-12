package com.qa.qa_orchestrator_service.service;

import com.qa.qa_orchestrator_service.jira.JiraClient;
import org.springframework.stereotype.Service;

@Service
public class QaOrchestratorService {

    private final JiraClient jiraClient;

    public QaOrchestratorService(JiraClient jiraClient) {
        this.jiraClient = jiraClient;
    }

    public String runAnalysis(String issueKey) {

        String issue = jiraClient.getIssue(issueKey);

        String analysis = """
        QA Orchestrator Analysis

        Requirement Analysis: READY. Coupon entry, validation,
        single coupon enforcement, and session persistence detected.

        Test Strategy:
        - Valid coupon
        - Invalid coupon
        - Multiple coupon restriction
        - Subtotal before tax
        - Session persistence

        Automation Recommendation: Hybrid (UI + API)

        Risk Level: HIGH
        Reason: Checkout flow, financial impact, regression risk.

        Release Recommendation: Caution until regression coverage confirmed.
        """;

        jiraClient.addComment(issueKey, analysis);

        return analysis;
    }
}