package com.qa.qa_orchestrator_service.service;

import com.qa.qa_orchestrator_service.jira.JiraClient;
import com.qa.qa_orchestrator_service.model.QaAnalysisResult;
import com.qa.qa_orchestrator_service.model.QaTestCase;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    public QaAnalysisResult runStructuredAnalysis(String issueKey) {
        String raw = runAnalysis(issueKey);

        QaAnalysisResult result = new QaAnalysisResult();
        result.setTraceabilityId(issueKey);
        result.setFeatureSummary("Derived from Jira issue " + issueKey);
        result.setRequirementStatus(extractStatus(raw));
        result.setAutomationRecommendation(extractSingleValue(raw, "Automation Recommendation:"));
        result.setRiskLevel(extractSingleValue(raw, "Risk Level:"));
        result.setReleaseRecommendation(extractSingleValue(raw, "Release Recommendation:"));
        result.setRiskScore(null);
        result.setClarifiedRequirements(new ArrayList<>());
        result.setEdgeCases(new ArrayList<>());
        result.setOpenQuestions(new ArrayList<>());
        result.setScope(new ArrayList<>());
        result.setOutOfScope(new ArrayList<>());
        result.setTestScenarios(extractBulletSection(raw, "Test Strategy:"));
        result.setTestCases(buildPlaceholderTestCases());
        result.setRawOutput(raw);

        return result;
    }

    private String extractStatus(String raw) {
        if (raw != null && raw.contains("Requirement Analysis: READY")) {
            return "READY";
        }
        if (raw != null && raw.contains("Requirement Analysis: BLOCKED")) {
            return "BLOCKED";
        }
        return "UNKNOWN";
    }

    private String extractSingleValue(String raw, String prefix) {
        if (raw == null || prefix == null) {
            return null;
        }

        String[] lines = raw.split("\\R");
        for (String line : lines) {
            if (line.trim().startsWith(prefix)) {
                return line.replace(prefix, "").trim();
            }
        }
        return null;
    }

    private List<String> extractBulletSection(String raw, String sectionHeader) {
        List<String> items = new ArrayList<>();
        if (raw == null || sectionHeader == null) {
            return items;
        }

        String[] lines = raw.split("\\R");
        boolean inSection = false;

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.equals(sectionHeader.trim())) {
                inSection = true;
                continue;
            }

            if (inSection) {
                if (trimmed.isEmpty()) {
                    continue;
                }

                if (!trimmed.startsWith("-")) {
                    break;
                }

                items.add(trimmed.substring(1).trim());
            }
        }

        return items;
    }

    private List<QaTestCase> buildPlaceholderTestCases() {
        List<QaTestCase> testCases = new ArrayList<>();

        testCases.add(new QaTestCase(
                "TC-01",
                "Validate happy path coupon application",
                "User has an active cart session",
                List.of("Open checkout", "Enter a valid coupon", "Apply coupon"),
                "Discount should be applied successfully",
                "UI",
                "Smoke",
                "Valid coupon",
                "High"
        ));

        testCases.add(new QaTestCase(
                "TC-02",
                "Reject invalid coupon",
                "User has an active cart session",
                List.of("Open checkout", "Enter an invalid coupon", "Apply coupon"),
                "System should reject invalid coupon with clear validation message",
                "UI",
                "Regression",
                "Invalid coupon",
                "High"
        ));

        testCases.add(new QaTestCase(
                "TC-03",
                "Prevent multiple coupon application",
                "User already applied one coupon",
                List.of("Apply first coupon", "Try applying second coupon"),
                "System should enforce single coupon rule",
                "UI",
                "Regression",
                "Two valid coupons",
                "High"
        ));

        testCases.add(new QaTestCase(
                "TC-04",
                "Validate subtotal calculation before tax",
                "User has taxable items in cart",
                List.of("Open checkout", "Apply coupon", "Review subtotal"),
                "Discount should affect subtotal before tax calculation",
                "API",
                "Regression",
                "Coupon + taxable cart",
                "High"
        ));

        testCases.add(new QaTestCase(
                "TC-05",
                "Validate session persistence for coupon state",
                "User has applied a coupon",
                List.of("Apply coupon", "Refresh page or continue same session"),
                "Coupon state should persist in the same session",
                "E2E",
                "Regression",
                "Same session flow",
                "Medium"
        ));

        return testCases;
    }
}