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

    public QaAnalysisResult buildStructuredAnalysis(String issueKey, String raw) {
        QaAnalysisResult result = new QaAnalysisResult();
        result.setTraceabilityId(issueKey);
        result.setFeatureSummary("Derived from Jira issue " + issueKey);
        result.setRequirementStatus(extractStatus(raw));
        result.setAutomationRecommendation(extractSingleValue(raw, "Automation Recommendation:"));

        String rawRiskLevel = extractSingleValue(raw, "Risk Level:");
        String riskReason = extractSingleValue(raw, "Reason:");

        int riskScore = calculateRiskScore(raw, rawRiskLevel);

        result.setRiskScore(riskScore);
        result.setRiskLevel(mapRiskLevel(riskScore, rawRiskLevel));
        result.setRiskReason(riskReason);
        result.setTopRiskDrivers(buildTopRiskDrivers(raw));
        result.setRawReleaseRecommendation(extractSingleValue(raw, "Release Recommendation:"));
        result.setReleaseRecommendation(mapReleaseRecommendation(riskScore));

        result.setClarifiedRequirements(buildClarifiedRequirements(raw));
        result.setEdgeCases(buildEdgeCases(raw));
        result.setOpenQuestions(buildOpenQuestions(raw));
        result.setScope(buildScope(raw));
        result.setOutOfScope(buildOutOfScope(raw));

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
                "High"));

        testCases.add(new QaTestCase(
                "TC-02",
                "Reject invalid coupon",
                "User has an active cart session",
                List.of("Open checkout", "Enter an invalid coupon", "Apply coupon"),
                "System should reject invalid coupon with clear validation message",
                "UI",
                "Regression",
                "Invalid coupon",
                "High"));

        testCases.add(new QaTestCase(
                "TC-03",
                "Prevent multiple coupon application",
                "User already applied one coupon",
                List.of("Apply first coupon", "Try applying second coupon"),
                "System should enforce single coupon rule",
                "UI",
                "Regression",
                "Two valid coupons",
                "High"));

        testCases.add(new QaTestCase(
                "TC-04",
                "Validate subtotal calculation before tax",
                "User has taxable items in cart",
                List.of("Open checkout", "Apply coupon", "Review subtotal"),
                "Discount should affect subtotal before tax calculation",
                "API",
                "Regression",
                "Coupon + taxable cart",
                "High"));

        testCases.add(new QaTestCase(
                "TC-05",
                "Validate session persistence for coupon state",
                "User has applied a coupon",
                List.of("Apply coupon", "Refresh page or continue same session"),
                "Coupon state should persist in the same session",
                "E2E",
                "Regression",
                "Same session flow",
                "Medium"));

        return testCases;
    }

    private List<String> buildTopRiskDrivers(String raw) {
        List<String> drivers = new ArrayList<>();

        String reason = extractSingleValue(raw, "Reason:");
        if (reason != null && !reason.isBlank()) {
            String[] parts = reason.split(",");
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    drivers.add(trimmed);
                }
            }
        }

        return drivers;
    }

    private int calculateRiskScore(String raw, String rawRiskLevel) {
        int score = 0;
        String lower = raw == null ? "" : raw.toLowerCase();

        if (lower.contains("checkout") || lower.contains("payment") || lower.contains("login")) {
            score += 20;
        }

        if (lower.contains("regression")) {
            score += 15;
        }

        if (lower.contains("session persistence") || lower.contains("session") || lower.contains("state")) {
            score += 10;
        }

        if (lower.contains("financial impact") || lower.contains("subtotal") || lower.contains("tax")
                || lower.contains("discount") || lower.contains("coupon")) {
            score += 15;
        }

        if (lower.contains("multiple coupon") || lower.contains("restriction") || lower.contains("validation")) {
            score += 10;
        }

        if ("HIGH".equalsIgnoreCase(rawRiskLevel) && score < 70) {
            score = 70;
        } else if ("MEDIUM".equalsIgnoreCase(rawRiskLevel) && score < 40) {
            score = 40;
        }

        return Math.min(score, 100);
    }

    private String mapRiskLevel(int riskScore, String fallbackLevel) {
        if (riskScore >= 70) {
            return "HIGH";
        }
        if (riskScore >= 40) {
            return "MEDIUM";
        }
        if (riskScore >= 0) {
            return "LOW";
        }
        return fallbackLevel != null ? fallbackLevel : "UNKNOWN";
    }

    private String mapReleaseRecommendation(int riskScore) {
        if (riskScore >= 70) {
            return "Block";
        }
        if (riskScore >= 40) {
            return "Caution";
        }
        return "Go";
    }

    private List<String> buildClarifiedRequirements(String raw) {
        List<String> items = new ArrayList<>();
        String lower = raw == null ? "" : raw.toLowerCase();

        if (lower.contains("coupon")) {
            items.add("User can enter and apply a coupon code during checkout.");
        }
        if (lower.contains("validation")) {
            items.add("Coupon input must be validated before discount is applied.");
        }
        if (lower.contains("single coupon")) {
            items.add("Only one coupon can be applied per checkout session.");
        }
        if (lower.contains("session persistence")) {
            items.add("Applied coupon state must persist within the same session.");
        }

        return items;
    }

    private List<String> buildEdgeCases(String raw) {
        List<String> items = new ArrayList<>();
        String lower = raw == null ? "" : raw.toLowerCase();

        if (lower.contains("invalid coupon")) {
            items.add("Invalid coupon code is entered.");
        }
        if (lower.contains("multiple coupon")) {
            items.add("User attempts to apply a second coupon after one is already active.");
        }
        if (lower.contains("subtotal")) {
            items.add("Discount affects subtotal before tax calculation.");
        }
        if (lower.contains("session")) {
            items.add("Coupon remains applied after refresh within the same session.");
        }

        return items;
    }

    private List<String> buildOpenQuestions(String raw) {
        return new ArrayList<>();
    }

    private List<String> buildScope(String raw) {
        List<String> items = new ArrayList<>();
        String lower = raw == null ? "" : raw.toLowerCase();

        if (lower.contains("coupon")) {
            items.add("Coupon entry and application flow");
        }
        if (lower.contains("validation")) {
            items.add("Coupon validation behavior");
        }
        if (lower.contains("multiple coupon")) {
            items.add("Single coupon enforcement");
        }
        if (lower.contains("subtotal")) {
            items.add("Discount impact on subtotal before tax");
        }
        if (lower.contains("session")) {
            items.add("Same-session coupon persistence");
        }

        return items;
    }

    private List<String> buildOutOfScope(String raw) {
        List<String> items = new ArrayList<>();
        items.add("Cross-device persistence");
        items.add("Coupon management admin flows");
        items.add("Promotion creation and configuration");
        return items;
    }
}