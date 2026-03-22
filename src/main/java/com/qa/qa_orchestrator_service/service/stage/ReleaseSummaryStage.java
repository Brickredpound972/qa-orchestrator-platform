package com.qa.qa_orchestrator_service.service.stage;

import com.qa.qa_orchestrator_service.repository.AnalysisRecord;
import com.qa.qa_orchestrator_service.service.llm.LlmClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ReleaseSummaryStage
 *
 * Triggered when a Jira ticket moves to "Done".
 * Reads the full analysis history for that ticket from DB
 * and produces a final QA release summary via LLM.
 */
@Component
public class ReleaseSummaryStage {

    private static final Logger log = LoggerFactory.getLogger(ReleaseSummaryStage.class);

    private static final String SYSTEM_PROMPT = """
            You are a senior QA engineer writing a final release summary for a software feature.
            You will receive the QA analysis history for a Jira ticket that has just been marked as Done.
            Your job is to produce a concise, professional QA release summary.

            The summary should cover:
            - What was the feature
            - What was the risk level and why
            - What test coverage was generated
            - Were there any open questions that should have been resolved before release
            - Final QA verdict: was this feature adequately covered before release?

            Format the output as plain text, not JSON.
            Use clear sections with headers.
            Keep it concise — max 300 words.
            Start with a one-line verdict: APPROVED, APPROVED WITH RISK, or RELEASED WITHOUT FULL COVERAGE.
            """;

    private final LlmClient llmClient;

    public ReleaseSummaryStage(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    public String generateSummary(String issueKey, List<AnalysisRecord> history) {
        try {
            String context = buildContext(issueKey, history);
            String summary = llmClient.call(SYSTEM_PROMPT, context);
            log.info("[RELEASE] Summary generated for {}", issueKey);
            return summary;
        } catch (Exception e) {
            log.warn("[RELEASE] Failed to generate summary for {}: {}", issueKey, e.getMessage());
            return buildFallbackSummary(issueKey, history);
        }
    }

    private String buildContext(String issueKey, List<AnalysisRecord> history) {
        if (history == null || history.isEmpty()) {
            return "Issue: " + issueKey + "\nNo prior analysis found for this ticket.";
        }

        AnalysisRecord latest = history.get(0);
        StringBuilder ctx = new StringBuilder();
        ctx.append("Issue: ").append(issueKey).append("\n");
        ctx.append("Feature: ").append(latest.getFeatureSummary()).append("\n");
        ctx.append("Total analyses: ").append(history.size()).append("\n");
        ctx.append("Latest risk level: ").append(latest.getRiskLevel()).append("\n");
        ctx.append("Latest risk score: ").append(latest.getRiskScore()).append("\n");
        ctx.append("Latest release recommendation: ").append(latest.getReleaseRecommendation()).append("\n");
        ctx.append("Test cases generated: ").append(latest.getTestCaseCount()).append("\n");
        ctx.append("Automation strategy: ").append(latest.getAutomationRecommendation()).append("\n");

        ctx.append("\nAnalysis history (newest first):\n");
        for (AnalysisRecord record : history) {
            ctx.append("- ").append(record.getAnalyzedAt())
               .append(" | risk=").append(record.getRiskLevel())
               .append(" | score=").append(record.getRiskScore())
               .append(" | release=").append(record.getReleaseRecommendation())
               .append("\n");
        }

        return ctx.toString();
    }

    private String buildFallbackSummary(String issueKey, List<AnalysisRecord> history) {
        if (history == null || history.isEmpty()) {
            return "QA Release Summary — " + issueKey + "\n\nNo prior QA analysis found. Feature released without QA coverage.";
        }

        AnalysisRecord latest = history.get(0);
        return "QA Release Summary — " + issueKey + "\n\n"
                + "Feature: " + latest.getFeatureSummary() + "\n"
                + "Risk Level: " + latest.getRiskLevel() + " (" + latest.getRiskScore() + ")\n"
                + "Release Recommendation: " + latest.getReleaseRecommendation() + "\n"
                + "Test Cases: " + latest.getTestCaseCount() + "\n"
                + "Analyses performed: " + history.size() + "\n\n"
                + "Note: Automated summary generation failed. Manual review recommended.";
    }
}