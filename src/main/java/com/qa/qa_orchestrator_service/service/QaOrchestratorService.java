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
        String analysis = buildAnalysisFromIssue(issueKey, issue);
        jiraClient.addComment(issueKey, analysis);
        return analysis;
    }

    public QaAnalysisResult buildStructuredAnalysis(String issueKey, String raw) {
        QaAnalysisResult result = new QaAnalysisResult();
        result.setTraceabilityId(issueKey);
        result.setFeatureSummary(buildFeatureSummary(raw));
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
        result.setTestCases(buildGeneratedTestCases(result));
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

        if (lower.contains("financial impact")
                || lower.contains("subtotal")
                || lower.contains("tax")
                || lower.contains("discount")
                || lower.contains("coupon")) {
            score += 15;
        }

        if (lower.contains("multiple coupon")
                || lower.contains("restriction")
                || lower.contains("validation")) {
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
        if (lower.contains("single coupon")
                || lower.contains("multiple coupon")
                || lower.contains("one coupon")) {
            items.add("Only one coupon can be applied per checkout session.");
        }
        if (lower.contains("session persistence")
                || lower.contains("same session")
                || lower.contains("session")) {
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
        List<String> items = new ArrayList<>();
        String lower = raw == null ? "" : raw.toLowerCase();

        if (!lower.contains("expiry") && lower.contains("coupon")) {
            items.add("Should expired coupons be rejected with a specific validation message?");
        }

        if (!lower.contains("stack") && lower.contains("coupon")) {
            items.add("Is coupon stacking always disallowed, or only for this checkout flow?");
        }

        if (!lower.contains("guest") && lower.contains("checkout")) {
            items.add("Does the coupon behavior differ for guest and authenticated users?");
        }

        if (!lower.contains("currency") && (lower.contains("subtotal") || lower.contains("discount"))) {
            items.add("Are there any currency, rounding, or localization rules affecting subtotal calculation?");
        }

        return items;
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

    private List<QaTestCase> buildGeneratedTestCases(QaAnalysisResult result) {
        List<QaTestCase> testCases = new ArrayList<>();

        List<String> scenarios = result.getTestScenarios() != null
                ? result.getTestScenarios()
                : new ArrayList<>();

        List<String> edgeCases = result.getEdgeCases() != null
                ? result.getEdgeCases()
                : new ArrayList<>();

        int tcIndex = 1;

        for (String scenario : scenarios) {
            String normalized = scenario.toLowerCase();

            if (normalized.contains("invalid")) {
                testCases.add(new QaTestCase(
                        formatTestCaseId(tcIndex++),
                        "Reject invalid coupon code",
                        "User has items in cart and an active checkout session",
                        List.of("Open checkout", "Enter an invalid coupon", "Apply coupon"),
                        "System should reject the coupon and display validation feedback",
                        "UI",
                        "Regression",
                        "Invalid coupon",
                        "High"));
            } else if (normalized.contains("valid")) {
                testCases.add(new QaTestCase(
                        formatTestCaseId(tcIndex++),
                        "Validate valid coupon application",
                        "User has items in cart and an active checkout session",
                        List.of("Open checkout", "Enter a valid coupon", "Apply coupon"),
                        "System should accept the coupon and apply the discount",
                        "UI",
                        "Smoke",
                        "Valid coupon",
                        "High"));
            } else if (normalized.contains("multiple")) {
                testCases.add(new QaTestCase(
                        formatTestCaseId(tcIndex++),
                        "Prevent multiple coupon application",
                        "User already applied one valid coupon",
                        List.of("Apply first coupon", "Attempt to apply a second coupon"),
                        "System should enforce the single coupon restriction",
                        "UI",
                        "Regression",
                        "Two valid coupons",
                        "High"));
            } else if (normalized.contains("subtotal")) {
                testCases.add(new QaTestCase(
                        formatTestCaseId(tcIndex++),
                        "Validate subtotal calculation before tax",
                        "User has taxable items in cart and a valid coupon",
                        List.of("Open checkout", "Apply valid coupon", "Review subtotal before tax"),
                        "Discount should be reflected in subtotal before tax is calculated",
                        "API",
                        "Regression",
                        "Coupon + taxable cart",
                        "High"));
            } else if (normalized.contains("session")) {
                testCases.add(new QaTestCase(
                        formatTestCaseId(tcIndex++),
                        "Validate coupon persistence in same session",
                        "User has successfully applied a coupon",
                        List.of("Apply coupon", "Refresh the page within the same session"),
                        "Coupon state should persist during the same user session",
                        "E2E",
                        "Regression",
                        "Same session",
                        "Medium"));
            }
        }

        for (String edgeCase : edgeCases) {
            String normalized = edgeCase.toLowerCase();

            if (normalized.contains("invalid coupon")
                    && !containsTitle(testCases, "Reject invalid coupon code")) {
                testCases.add(new QaTestCase(
                        formatTestCaseId(tcIndex++),
                        "Reject invalid coupon code",
                        "User has items in cart and an active checkout session",
                        List.of("Open checkout", "Enter an invalid coupon", "Apply coupon"),
                        "System should reject the coupon and display validation feedback",
                        "UI",
                        "Regression",
                        "Invalid coupon",
                        "High"));
            }

            if (normalized.contains("second coupon")
                    && !containsTitle(testCases, "Prevent multiple coupon application")) {
                testCases.add(new QaTestCase(
                        formatTestCaseId(tcIndex++),
                        "Prevent multiple coupon application",
                        "User already applied one valid coupon",
                        List.of("Apply first coupon", "Attempt to apply a second coupon"),
                        "System should enforce the single coupon restriction",
                        "UI",
                        "Regression",
                        "Two valid coupons",
                        "High"));
            }

            if (normalized.contains("subtotal")
                    && !containsTitle(testCases, "Validate subtotal calculation before tax")) {
                testCases.add(new QaTestCase(
                        formatTestCaseId(tcIndex++),
                        "Validate subtotal calculation before tax",
                        "User has taxable items in cart and a valid coupon",
                        List.of("Open checkout", "Apply valid coupon", "Review subtotal before tax"),
                        "Discount should be reflected in subtotal before tax is calculated",
                        "API",
                        "Regression",
                        "Coupon + taxable cart",
                        "High"));
            }

            if ((normalized.contains("same session") || normalized.contains("refresh"))
                    && !containsTitle(testCases, "Validate coupon persistence in same session")) {
                testCases.add(new QaTestCase(
                        formatTestCaseId(tcIndex++),
                        "Validate coupon persistence in same session",
                        "User has successfully applied a coupon",
                        List.of("Apply coupon", "Refresh the page within the same session"),
                        "Coupon state should persist during the same user session",
                        "E2E",
                        "Regression",
                        "Same session",
                        "Medium"));
            }
        }

        return testCases;
    }

    private String formatTestCaseId(int index) {
        return String.format("TC-%02d", index);
    }

    private boolean containsTitle(List<QaTestCase> testCases, String title) {
        for (QaTestCase testCase : testCases) {
            if (testCase.getTitle() != null && testCase.getTitle().equalsIgnoreCase(title)) {
                return true;
            }
        }
        return false;
    }

    private String buildAnalysisFromIssue(String issueKey, String issue) {
        String lower = issue == null ? "" : issue.toLowerCase();

        List<String> scenarios = deriveScenariosFromIssue(lower);
        String requirementStatus = deriveRequirementStatus(lower);
        String automationRecommendation = deriveAutomationRecommendation(lower);
        String riskLevel = deriveRiskLevel(lower);
        String riskReason = deriveRiskReason(lower);
        String releaseRecommendation = deriveRawReleaseRecommendation(lower, riskLevel);

        StringBuilder builder = new StringBuilder();
        builder.append("QA Orchestrator Analysis\n\n");
        builder.append("Requirement Analysis: ").append(requirementStatus).append(". ");

        if ("READY".equals(requirementStatus)) {
            builder.append("Requirements appear testable based on available issue context.\n\n");
        } else {
            builder.append("Critical testing details are missing from the issue context.\n\n");
        }

        builder.append("Test Strategy:\n");
        for (String scenario : scenarios) {
            builder.append("- ").append(scenario).append("\n");
        }

        builder.append("\n");
        builder.append("Automation Recommendation: ").append(automationRecommendation).append("\n\n");
        builder.append("Risk Level: ").append(riskLevel).append("\n");
        builder.append("Reason: ").append(riskReason).append("\n\n");
        builder.append("Release Recommendation: ").append(releaseRecommendation);

        return builder.toString();
    }

    private List<String> deriveScenariosFromIssue(String lower) {
        List<String> scenarios = new ArrayList<>();

        if (lower.contains("coupon")) {
            scenarios.add("Valid coupon");
            scenarios.add("Invalid coupon");
        }

        if (lower.contains("single") || lower.contains("multiple coupon") || lower.contains("one coupon")) {
            scenarios.add("Multiple coupon restriction");
        }

        if (lower.contains("subtotal") || lower.contains("tax") || lower.contains("discount")) {
            scenarios.add("Subtotal before tax");
        }

        if (lower.contains("session") || lower.contains("persist")) {
            scenarios.add("Session persistence");
        }

        if (scenarios.isEmpty()) {
            scenarios.add("Core happy path");
            scenarios.add("Negative validation path");
        }

        return scenarios;
    }

    private String deriveRequirementStatus(String lower) {
        if (lower.isBlank()) {
            return "BLOCKED";
        }
        return "READY";
    }

    private String deriveAutomationRecommendation(String lower) {
        if (lower == null || lower.isBlank()) {
            return "Manual";
        }

        boolean hasUiSignals = lower.contains("checkout")
                || lower.contains("coupon")
                || lower.contains("session")
                || lower.contains("user")
                || lower.contains("page")
                || lower.contains("ui");

        boolean hasApiSignals = lower.contains("api")
                || lower.contains("service")
                || lower.contains("subtotal")
                || lower.contains("tax")
                || lower.contains("discount")
                || lower.contains("validation");

        if (hasUiSignals && hasApiSignals) {
            return "Hybrid (UI + API)";
        }
        if (hasApiSignals) {
            return "Automation (API-heavy)";
        }
        if (hasUiSignals) {
            return "Automation (UI-heavy)";
        }

        return "Automation";
    }

    private String deriveRiskLevel(String lower) {
        int score = 0;

        if (lower.contains("checkout") || lower.contains("payment") || lower.contains("login")) {
            score += 20;
        }
        if (lower.contains("session") || lower.contains("persist")) {
            score += 10;
        }
        if (lower.contains("subtotal") || lower.contains("tax") || lower.contains("discount")
                || lower.contains("coupon")) {
            score += 15;
        }
        if (lower.contains("regression") || lower.contains("existing")) {
            score += 15;
        }

        if (score >= 40) {
            return "HIGH";
        }
        if (score >= 20) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private String deriveRiskReason(String lower) {
        List<String> reasons = new ArrayList<>();

        if (lower.contains("checkout")) {
            reasons.add("Checkout flow");
        }
        if (lower.contains("payment") || lower.contains("subtotal") || lower.contains("tax")
                || lower.contains("discount")) {
            reasons.add("Financial impact");
        }
        if (lower.contains("session") || lower.contains("persist")) {
            reasons.add("Session/state behavior");
        }
        if (lower.contains("coupon")) {
            reasons.add("Promotion validation complexity");
        }

        if (reasons.isEmpty()) {
            reasons.add("General feature risk");
        }

        return String.join(", ", reasons);
    }

    private String deriveRawReleaseRecommendation(String lower, String riskLevel) {
        if ("HIGH".equalsIgnoreCase(riskLevel)) {
            return "Caution until regression coverage confirmed.";
        }
        if ("MEDIUM".equalsIgnoreCase(riskLevel)) {
            return "Proceed with controlled validation.";
        }
        return "Go with standard validation.";
    }

    private String buildFeatureSummary(String raw) {
        String lower = raw == null ? "" : raw.toLowerCase();

        if (lower.contains("coupon") && lower.contains("checkout")) {
            return "Coupon validation and application behavior during checkout.";
        }
        if (lower.contains("coupon")) {
            return "Coupon application and validation flow.";
        }
        if (lower.contains("checkout")) {
            return "Checkout-related validation and session behavior.";
        }

        return "QA analysis generated from available issue context.";
    }
}