package com.qa.qa_orchestrator_service.service;

import com.qa.qa_orchestrator_service.jira.JiraClient;
import com.qa.qa_orchestrator_service.model.QaAnalysisResult;
import com.qa.qa_orchestrator_service.service.stage.RequirementAnalysisStage;
import com.qa.qa_orchestrator_service.service.stage.RiskAnalysisStage;
import com.qa.qa_orchestrator_service.service.stage.TestDesignStage;
import com.qa.qa_orchestrator_service.service.stage.AutomationDecisionStage;
import com.qa.qa_orchestrator_service.service.stage.AnalysisSummaryStage;
import com.qa.qa_orchestrator_service.service.stage.StageAggregationStage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QaOrchestratorService {

    private final JiraClient jiraClient;
    private final RequirementAnalysisStage requirementAnalysisStage;
    private final TestDesignStage testDesignStage;
    private final RiskAnalysisStage riskAnalysisStage;
    private final AutomationDecisionStage automationDecisionStage;
    private final AnalysisSummaryStage analysisSummaryStage;
    private final StageAggregationStage stageAggregationStage;

    public QaOrchestratorService(
            JiraClient jiraClient,
            RequirementAnalysisStage requirementAnalysisStage,
            TestDesignStage testDesignStage,
            RiskAnalysisStage riskAnalysisStage,
            AutomationDecisionStage automationDecisionStage,
            AnalysisSummaryStage analysisSummaryStage,
            StageAggregationStage stageAggregationStage) {
        this.jiraClient = jiraClient;
        this.requirementAnalysisStage = requirementAnalysisStage;
        this.testDesignStage = testDesignStage;
        this.riskAnalysisStage = riskAnalysisStage;
        this.automationDecisionStage = automationDecisionStage;
        this.analysisSummaryStage = analysisSummaryStage;
        this.stageAggregationStage = stageAggregationStage;
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
        result.setAutomationRecommendation(extractSingleValue(raw, "Automation Recommendation:"));
        result.setRawOutput(raw);

        requirementAnalysisStage.apply(result, raw);
        testDesignStage.apply(result, raw);
        automationDecisionStage.apply(result, raw);
        riskAnalysisStage.apply(result, raw);
        analysisSummaryStage.apply(result);
        stageAggregationStage.apply(result);

        return result;
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
        if (lower == null || lower.isBlank()) {
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
        if (lower.contains("subtotal")
                || lower.contains("tax")
                || lower.contains("discount")
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
        if (lower.contains("payment")
                || lower.contains("subtotal")
                || lower.contains("tax")
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
}