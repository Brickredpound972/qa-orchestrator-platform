package com.qa.qa_orchestrator_service.service.stage;

import com.qa.qa_orchestrator_service.model.QaAnalysisResult;
import org.springframework.stereotype.Component;

@Component
public class AnalysisSummaryStage {

    public void apply(QaAnalysisResult result) {
        String summary = String.format(
                "Requirement status: %s. Automation: %s. Risk: %s (%s). Release decision: %s.",
                safe(result.getRequirementStatus()),
                safe(result.getAutomationRecommendation()),
                safe(result.getRiskLevel()),
                result.getRiskScore() != null ? result.getRiskScore() : "N/A",
                safe(result.getReleaseRecommendation()));

        result.setAnalysisSummary(summary);
    }

    private String safe(String value) {
        return value != null ? value : "UNKNOWN";
    }
}