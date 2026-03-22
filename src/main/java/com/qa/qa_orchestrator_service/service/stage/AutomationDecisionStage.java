package com.qa.qa_orchestrator_service.service.stage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qa.qa_orchestrator_service.service.llm.LlmClient;
import com.qa.qa_orchestrator_service.model.QaAnalysisResult;
import com.qa.qa_orchestrator_service.model.AutomationStageArtifact;
import org.springframework.stereotype.Component;

@Component
public class AutomationDecisionStage {

    private static final String SYSTEM_PROMPT = """
            You are a senior QA automation architect deciding the automation strategy for a software feature.
            You will receive test case information and risk indicators.
            Your job is to recommend the best automation approach as JSON.
            RULES:
            - Return ONLY valid JSON. No markdown, no explanation, no code blocks.
            - automationRecommendation must be one of: "Hybrid (UI + API)", "Automation (UI-heavy)", "Automation (API-heavy)", "Manual"
            - coverageSplit format: "UI X% / API Y%" where X + Y = 100
            - frameworkSuggestion must be a real, commonly used test framework
            REQUIRED JSON STRUCTURE:
            {
              "automationRecommendation": "Hybrid (UI + API)",
              "automationReasoning": "explanation of why this approach was chosen",
              "coverageSplit": "UI 60% / API 40%",
              "frameworkSuggestion": "Java + Selenium + TestNG + REST Assured"
            }
            """;

    private final LlmClient llmClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AutomationDecisionStage(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    public void apply(QaAnalysisResult result, String jiraIssueJson) {
        try {
            String context = buildContext(result);
            String response = llmClient.call(SYSTEM_PROMPT, context);
            AutomationStageArtifact artifact = parseResponse(response);

            result.setAutomationRecommendation(artifact.getAutomationRecommendation());
            result.setAutomationReasoning(artifact.getAutomationReasoning());
            result.setCoverageSplit(artifact.getCoverageSplit());
            result.setFrameworkSuggestion(artifact.getFrameworkSuggestion());
            result.setAutomationStage(artifact);

        } catch (Exception e) {
            AutomationStageArtifact fallback = new AutomationStageArtifact();
            fallback.setAutomationRecommendation("Hybrid (UI + API)");
            fallback.setAutomationReasoning("Fallback: could not determine automation strategy.");
            fallback.setCoverageSplit("UI 50% / API 50%");
            fallback.setFrameworkSuggestion("Java + Selenium + TestNG + REST Assured");
            result.setAutomationStage(fallback);
        }
    }

    private String buildContext(QaAnalysisResult result) {
        long uiCount = result.getTestCases() != null
                ? result.getTestCases().stream().filter(tc -> "UI".equals(tc.getTestType())).count() : 0;
        long apiCount = result.getTestCases() != null
                ? result.getTestCases().stream().filter(tc -> "API".equals(tc.getTestType())).count() : 0;
        long e2eCount = result.getTestCases() != null
                ? result.getTestCases().stream().filter(tc -> "E2E".equals(tc.getTestType())).count() : 0;

        return "Feature: " + result.getFeatureSummary() + "\n"
                + "Test case distribution — UI: " + uiCount + ", API: " + apiCount + ", E2E: " + e2eCount + "\n"
                + "Risk level: " + result.getRiskLevel() + "\n"
                + "Scenarios: " + result.getTestScenarios();
    }

    private AutomationStageArtifact parseResponse(String raw) {
        try {
            String cleaned = raw.replaceAll("(?s)```json\\s*", "").replaceAll("(?s)```\\s*", "").trim();
            JsonNode node = objectMapper.readTree(cleaned);
            AutomationStageArtifact artifact = new AutomationStageArtifact();
            artifact.setAutomationRecommendation(node.path("automationRecommendation").asText("Hybrid (UI + API)"));
            artifact.setAutomationReasoning(node.path("automationReasoning").asText(""));
            artifact.setCoverageSplit(node.path("coverageSplit").asText("UI 50% / API 50%"));
            artifact.setFrameworkSuggestion(node.path("frameworkSuggestion").asText("Java + Selenium + TestNG + REST Assured"));
            return artifact;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse automation response: " + e.getMessage(), e);
        }
    }
}