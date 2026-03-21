package com.qa.qa_orchestrator_service.service.stage;

import com.qa.qa_orchestrator_service.model.QaAnalysisResult;
import com.qa.qa_orchestrator_service.model.QaTestCase;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AnalysisSummaryStage
 *
 * Generates two outputs:
 * 1. analysisSummary — one-line summary for quick reference
 * 2. formattedOutput — full structured text for Copilot Studio consumption
 *
 * Copilot Studio cannot reliably read nested JSON fields.
 * The formattedOutput solves this by putting all data into a readable string
 * that Copilot can directly use in its response.
 */
@Component
public class AnalysisSummaryStage {

    public void apply(QaAnalysisResult result) {
        // One-line summary
        String summary = String.format(
                "Requirement status: %s. Automation: %s. Risk: %s (%s). Release decision: %s.",
                safe(result.getRequirementStatus()),
                safe(result.getAutomationRecommendation()),
                safe(result.getRiskLevel()),
                result.getRiskScore() != null ? result.getRiskScore() : "N/A",
                safe(result.getReleaseRecommendation()));

        result.setAnalysisSummary(summary);

        // Full formatted output for Copilot Studio
        result.setRawOutput(buildFormattedOutput(result));
    }

    private String buildFormattedOutput(QaAnalysisResult result) {
        StringBuilder sb = new StringBuilder();

        sb.append("========================================\n");
        sb.append("TRACEABILITY ID: ").append(safe(result.getTraceabilityId())).append("\n\n");

        // STAGE 0
        sb.append("STAGE 0 — TICKET CONTEXT\n");
        sb.append("Feature Summary: ").append(safe(result.getFeatureSummary())).append("\n");
        sb.append("Risk Indicators: ").append(safe(result.getRiskLevel())).append(" risk\n\n");

        // STAGE 1
        sb.append("----------------------------------------\n");
        sb.append("STAGE 1 — REQUIREMENT ANALYSIS\n");
        sb.append("Status: ").append(safe(result.getRequirementStatus())).append("\n");
        sb.append("Clarified Requirements:\n");
        appendList(sb, result.getClarifiedRequirements());
        sb.append("Edge Cases:\n");
        appendList(sb, result.getEdgeCases());
        sb.append("Open Questions:\n");
        appendList(sb, result.getOpenQuestions());
        sb.append("Scope:\n");
        appendList(sb, result.getScope());
        sb.append("Out of Scope:\n");
        appendList(sb, result.getOutOfScope());
        sb.append("\n");

        if ("BLOCKED".equalsIgnoreCase(result.getRequirementStatus())) {
            sb.append("Pipeline stopped — requirement status is BLOCKED.\n");
            return sb.toString();
        }

        // STAGE 2
        sb.append("----------------------------------------\n");
        sb.append("STAGE 2 — TEST STRATEGY\n");
        sb.append("Test Scenarios:\n");
        appendList(sb, result.getTestScenarios());
        sb.append("Test Cases:\n");
        if (result.getTestCases() != null) {
            for (QaTestCase tc : result.getTestCases()) {
                sb.append("  ").append(tc.getId()).append(": ").append(tc.getTitle()).append("\n");
                sb.append("  Preconditions: ").append(safe(tc.getPreconditions())).append("\n");
                sb.append("  Steps:\n");
                if (tc.getSteps() != null) {
                    for (int i = 0; i < tc.getSteps().size(); i++) {
                        sb.append("    ").append(i + 1).append(". ").append(tc.getSteps().get(i)).append("\n");
                    }
                }
                sb.append("  Expected Result: ").append(safe(tc.getExpectedResult())).append("\n");
                sb.append("  Test Type: ").append(safe(tc.getTestType())).append("\n");
                sb.append("  Suite Tag: ").append(safe(tc.getSuiteTag())).append("\n");
                sb.append("  Test Data: ").append(safe(tc.getTestData())).append("\n");
                sb.append("  Priority: ").append(safe(tc.getPriority())).append("\n\n");
            }
        }

        // STAGE 3
        sb.append("----------------------------------------\n");
        sb.append("STAGE 3 — AUTOMATION DECISION\n");
        sb.append("Automation Recommendation: ").append(safe(result.getAutomationRecommendation())).append("\n");
        sb.append("Reasoning: ").append(safe(result.getAutomationReasoning())).append("\n");
        sb.append("Coverage Split: ").append(safe(result.getCoverageSplit())).append("\n");
        sb.append("Framework: ").append(safe(result.getFrameworkSuggestion())).append("\n\n");

        // STAGE 5 — Bug Report
        sb.append("----------------------------------------\n");
        sb.append("STAGE 5 — BUG REPORT TEMPLATE\n");
        if (result.getBugReportStage() != null) {
            var bug = result.getBugReportStage();
            sb.append("Title: ").append(safe(bug.getTitle())).append("\n");
            sb.append("Environment: ").append(safe(bug.getEnvironment())).append("\n");
            sb.append("Severity: ").append(safe(bug.getSeverity())).append("\n");
            sb.append("Priority: ").append(safe(bug.getPriority())).append("\n");
            sb.append("Reproduction Steps:\n");
            appendList(sb, bug.getReproductionSteps());
            sb.append("Expected Result: ").append(safe(bug.getExpectedResult())).append("\n");
            sb.append("Actual Result: ").append(safe(bug.getActualResult())).append("\n");
            sb.append("Impact: ").append(safe(bug.getImpactSummary())).append("\n");
            sb.append("Affected Areas:\n");
            appendList(sb, bug.getAffectedAreas());
            sb.append("Suggested Assignee: ").append(safe(bug.getSuggestedAssignee())).append("\n\n");
        } else {
            sb.append("Not Applicable\n\n");
        }

        // STAGE 6
        sb.append("----------------------------------------\n");
        sb.append("STAGE 6 — RISK ANALYSIS\n");
        sb.append("Risk Score: ").append(result.getRiskScore() != null ? result.getRiskScore() : "N/A").append("\n");
        sb.append("Risk Level: ").append(safe(result.getRiskLevel())).append("\n");
        sb.append("Risk Reason: ").append(safe(result.getRiskReason())).append("\n");
        sb.append("Top Risk Drivers:\n");
        appendList(sb, result.getTopRiskDrivers());
        sb.append("Release Recommendation: ").append(safe(result.getReleaseRecommendation())).append("\n\n");

        sb.append("========================================\n");

        return sb.toString();
    }

    private void appendList(StringBuilder sb, List<String> items) {
        if (items != null && !items.isEmpty()) {
            for (String item : items) {
                sb.append("- ").append(item).append("\n");
            }
        } else {
            sb.append("- None identified\n");
        }
    }

    private String safe(String value) {
        return value != null ? value : "Not available";
    }
}