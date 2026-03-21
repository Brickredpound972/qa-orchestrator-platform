package com.qa.qa_orchestrator_service.service.stage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qa.qa_orchestrator_service.service.llm.GroqClient;
import com.qa.qa_orchestrator_service.model.QaAnalysisResult;
import com.qa.qa_orchestrator_service.model.AutomationStageArtifact;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AutomationDecisionStage — LLM-powered
 *
 * Decides HOW the feature should be automated based on:
 * - what the feature does (requirements)
 * - what test cases were generated (test design)
 * - what the risk level is (risk stage)
 *
 * KEY LESSON:
 * Automation strategy is not a fixed rule.
 * A high-risk API feature needs more API automation.
 * A UI-heavy feature with low backend complexity needs more UI automation.
 * The LLM reasons about this based on full pipeline context — not keyword matching.
 */
@Component
public class AutomationDecisionStage {

    private static final String SYSTEM_PROMPT = """
            You are a senior QA automation architect deciding the automation strategy for a feature.

            You will receive a summary of the feature including its requirements, test cases, risk level,
            and scope. Your job is to recommend the best automation approach.

            AUTOMATION OPTIONS:
            - "Manual" — feature is too unstable, exploratory, or ambiguous for automation
            - "Automation (API-heavy)" — backend logic dominates, minimal UI interaction needed
            - "Automation (UI-heavy)" — user-facing flows dominate, minimal API validation needed
            - "Hybrid (UI + API)" — both UI flows and backend logic require automated coverage

            FRAMEWORK OPTIONS (suggest based on test types):
            - UI only: Selenium + TestNG (Java) or Playwright (TypeScript)
            - API only: REST Assured + TestNG (Java) or Postman/Newman
            - Hybrid: Selenium + REST Assured + TestNG (Java)
            - E2E heavy: Cypress or Playwright

            RULES:
            - Return ONLY valid JSON. No markdown, no explanation, no code blocks.
            - automationRecommendation must be one of the four options above.
            - coverageSplit must reflect the UI vs API percentage as a string like "UI 60% / API 40%".
            - reasoning must explain WHY this strategy fits this specific feature.
            - frameworkSuggestion must be concrete and match the coverage split.

            REQUIRED JSON STRUCTURE:
            {
              "automationRecommendation": "Hybrid (UI + API)",
              "reasoning": "explanation of why this strategy fits this feature",
              "coverageSplit": "UI 60% / API 40%",
              "frameworkSuggestion": "Java + Selenium + TestNG + REST Assured"
            }
            """;

    private final GroqClient groqClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AutomationDecisionStage(GroqClient groqClient) {
        this.groqClient = groqClient;
    }

    public void apply(QaAnalysisResult result, String raw) {
        try {
            String stageInput = buildStageInput(result);
            String groqResponse = groqClient.call(SYSTEM_PROMPT, stageInput);
            applyParsedResponse(result, groqResponse);

        } catch (Exception e) {
            applyFallback(result);
            System.err.println("AutomationDecisionStage LLM error: " + e.getMessage());
        }
    }

    /**
     * Build input from ALL previous stage outputs.
     *
     * Automation strategy depends on:
     * - Feature type (from requirements)
     * - Test case types: UI / API / E2E ratio (from test design)
     * - Risk level (from risk stage) — high risk = more automation needed
     * - Scope boundaries (from requirements)
     */
    private String buildStageInput(QaAnalysisResult result) {
        StringBuilder input = new StringBuilder();

        input.append("Feature Summary: ")
             .append(safe(result.getFeatureSummary()))
             .append("\n\n");

        input.append("Risk Level: ")
             .append(safe(result.getRiskLevel()))
             .append("\n");

        input.append("Risk Score: ")
             .append(result.getRiskScore() != null ? result.getRiskScore() : "N/A")
             .append("\n\n");

        input.append("Clarified Requirements:\n");
        appendList(input, result.getClarifiedRequirements());

        input.append("\nScope:\n");
        appendList(input, result.getScope());

        input.append("\nTest Case Types Generated:\n");
        if (result.getTestCases() != null && !result.getTestCases().isEmpty()) {
            long uiCount = result.getTestCases().stream()
                    .filter(tc -> "UI".equalsIgnoreCase(tc.getTestType()))
                    .count();
            long apiCount = result.getTestCases().stream()
                    .filter(tc -> "API".equalsIgnoreCase(tc.getTestType()))
                    .count();
            long e2eCount = result.getTestCases().stream()
                    .filter(tc -> "E2E".equalsIgnoreCase(tc.getTestType()))
                    .count();

            input.append("- UI test cases: ").append(uiCount).append("\n");
            input.append("- API test cases: ").append(apiCount).append("\n");
            input.append("- E2E test cases: ").append(e2eCount).append("\n");
            input.append("- Total: ").append(result.getTestCases().size()).append("\n");
        } else {
            input.append("- No test cases available\n");
        }

        input.append("\nOpen Questions:\n");
        appendList(input, result.getOpenQuestions());

        return input.toString();
    }

    private void applyParsedResponse(QaAnalysisResult result, String raw) {
        try {
            String cleaned = raw
                    .replaceAll("(?s)```json\\s*", "")
                    .replaceAll("(?s)```\\s*", "")
                    .trim();

            JsonNode node = objectMapper.readTree(cleaned);

            String recommendation = node.path("automationRecommendation").asText("Hybrid (UI + API)");
            String reasoning = node.path("reasoning").asText("");
            String coverageSplit = node.path("coverageSplit").asText("UI 50% / API 50%");
            String frameworkSuggestion = node.path("frameworkSuggestion").asText("");

            result.setAutomationRecommendation(recommendation);
            result.setAutomationReasoning(reasoning);
            result.setCoverageSplit(coverageSplit);
            result.setFrameworkSuggestion(frameworkSuggestion);

            AutomationStageArtifact artifact = new AutomationStageArtifact();
            artifact.setAutomationRecommendation(recommendation);
            artifact.setAutomationReasoning(reasoning);
            artifact.setCoverageSplit(coverageSplit);
            artifact.setFrameworkSuggestion(frameworkSuggestion);

            result.setAutomationStage(artifact);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Groq automation response: " + e.getMessage(), e);
        }
    }

    private void applyFallback(QaAnalysisResult result) {
        result.setAutomationRecommendation("Hybrid (UI + API)");
        result.setAutomationReasoning("Automation strategy unavailable due to LLM error. Defaulting to Hybrid.");
        result.setCoverageSplit("UI 50% / API 50%");
        result.setFrameworkSuggestion("Java + Selenium + TestNG + REST Assured");

        AutomationStageArtifact artifact = new AutomationStageArtifact();
        artifact.setAutomationRecommendation("Hybrid (UI + API)");
        artifact.setAutomationReasoning("Automation strategy unavailable due to LLM error.");
        artifact.setCoverageSplit("UI 50% / API 50%");
        artifact.setFrameworkSuggestion("Java + Selenium + TestNG + REST Assured");
        result.setAutomationStage(artifact);
    }

    private void appendList(StringBuilder sb, List<String> items) {
        if (items != null && !items.isEmpty()) {
            for (String item : items) {
                sb.append("- ").append(item).append("\n");
            }
        } else {
            sb.append("- None\n");
        }
    }

    private String safe(String value) {
        return value != null ? value : "Not available";
    }
}