package com.qa.qa_orchestrator_service.service.stage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qa.qa_orchestrator_service.service.llm.LlmClient;
import com.qa.qa_orchestrator_service.model.QaAnalysisResult;
import com.qa.qa_orchestrator_service.model.RiskStageArtifact;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RiskAnalysisStage {

    private static final String SYSTEM_PROMPT = """
            You are a senior QA risk analyst evaluating the release risk of a software feature.
            You will receive full pipeline context including requirements, test cases, and automation strategy.
            Your job is to produce a structured risk assessment as JSON.
            RULES:
            - Return ONLY valid JSON. No markdown, no explanation, no code blocks.
            - riskScore must be an integer between 0 and 100.
            - riskLevel must be one of: LOW, MEDIUM, HIGH
              - HIGH: riskScore >= 70
              - MEDIUM: riskScore 40-69
              - LOW: riskScore < 40
            - releaseRecommendation must be one of: Go, Caution, Block
              - Block: riskScore >= 70
              - Caution: riskScore 40-69
              - Go: riskScore < 40
            - topRiskDrivers must be a list of the key reasons for the risk score.
            REQUIRED JSON STRUCTURE:
            {
              "riskScore": 75,
              "riskLevel": "HIGH",
              "riskReason": "brief explanation of overall risk",
              "topRiskDrivers": ["driver 1", "driver 2"],
              "releaseRecommendation": "Block"
            }
            """;

    private final LlmClient llmClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RiskAnalysisStage(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    public void apply(QaAnalysisResult result, String jiraIssueJson) {
        try {
            String context = buildContext(result);
            String response = llmClient.call(SYSTEM_PROMPT, context);
            RiskStageArtifact artifact = parseResponse(response);

            result.setRiskScore(artifact.getRiskScore());
            result.setRiskLevel(artifact.getRiskLevel());
            result.setRiskReason(artifact.getRiskReason());
            result.setTopRiskDrivers(artifact.getTopRiskDrivers());
            result.setReleaseRecommendation(artifact.getReleaseRecommendation());
            result.setRiskStage(artifact);

        } catch (Exception e) {
            RiskStageArtifact fallback = new RiskStageArtifact();
            fallback.setRiskScore(50);
            fallback.setRiskLevel("MEDIUM");
            fallback.setRiskReason("Risk analysis failed — defaulting to MEDIUM.");
            fallback.setTopRiskDrivers(List.of("LLM error: " + e.getMessage()));
            fallback.setReleaseRecommendation("Caution");
            result.setRiskStage(fallback);
        }
    }

    private String buildContext(QaAnalysisResult result) {
        return "Feature: " + result.getFeatureSummary() + "\n"
                + "Requirements: " + result.getClarifiedRequirements() + "\n"
                + "Edge Cases: " + result.getEdgeCases() + "\n"
                + "Open Questions: " + result.getOpenQuestions() + "\n"
                + "Test Cases Count: " + (result.getTestCases() != null ? result.getTestCases().size() : 0) + "\n"
                + "Automation: " + result.getAutomationRecommendation() + "\n"
                + "Coverage Split: " + result.getCoverageSplit();
    }

    private RiskStageArtifact parseResponse(String raw) {
        try {
            String cleaned = raw.replaceAll("(?s)```json\\s*", "").replaceAll("(?s)```\\s*", "").trim();
            JsonNode node = objectMapper.readTree(cleaned);
            RiskStageArtifact artifact = new RiskStageArtifact();
            artifact.setRiskScore(node.path("riskScore").asInt(50));
            artifact.setRiskLevel(node.path("riskLevel").asText("MEDIUM"));
            artifact.setRiskReason(node.path("riskReason").asText(""));
            artifact.setTopRiskDrivers(toStringList(node.path("topRiskDrivers")));
            artifact.setReleaseRecommendation(node.path("releaseRecommendation").asText("Caution"));
            return artifact;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse risk response: " + e.getMessage(), e);
        }
    }

    private List<String> toStringList(JsonNode arrayNode) {
        List<String> items = new ArrayList<>();
        if (arrayNode.isArray()) {
            for (JsonNode item : arrayNode) items.add(item.asText());
        }
        return items;
    }
}