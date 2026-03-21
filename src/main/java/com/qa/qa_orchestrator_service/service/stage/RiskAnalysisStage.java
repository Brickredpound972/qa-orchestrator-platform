package com.qa.qa_orchestrator_service.service.stage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qa.qa_orchestrator_service.service.llm.GroqClient;
import com.qa.qa_orchestrator_service.model.QaAnalysisResult;
import com.qa.qa_orchestrator_service.model.RiskStageArtifact;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * RiskAnalysisStage — LLM-powered
 *
 * Takes the full pipeline context (requirements + test cases + open questions)
 * and produces a structured risk evaluation with a numeric score.
 *
 * KEY LESSON:
 * This stage reads from ALL previous stages.
 * Risk cannot be calculated in isolation — it depends on:
 *   - what the feature does (requirement stage)
 *   - how many test cases were needed (test design stage)
 *   - what questions remain open (requirement stage)
 *
 * The LLM reasons about all of this together, like a senior QA engineer would.
 */
@Component
public class RiskAnalysisStage {

    private static final String SYSTEM_PROMPT = """
            You are a senior QA engineer performing release risk analysis.

            You will receive a summary of a feature including its requirements, test cases, and open questions.
            Your job is to evaluate the release risk and return a structured JSON object.

            SCORING RULES:
            Base Score = 0
            Add +20 if feature impacts a critical user path (checkout, payment, login, authentication)
            Add +15 if the feature is regression-sensitive or touches shared components
            Add +10 if session or state persistence is involved
            Add +15 if there are financial, billing, or calculation impacts
            Add +10 if there are unresolved open questions that affect test coverage
            Add +10 if the feature has more than 7 test cases (indicates complexity)
            Add +15 if there are integration dependencies with external systems
            Cap total at 100.

            RISK MAPPING:
            0-39   = LOW
            40-69  = MEDIUM
            70-100 = HIGH

            RELEASE MAPPING:
            LOW    = Go
            MEDIUM = Caution
            HIGH   = Block

            RULES:
            - Return ONLY valid JSON. No markdown, no explanation, no code blocks.
            - riskScore must be an integer between 0 and 100.
            - topRiskDrivers must be specific reasons, not generic labels.
            - mitigationSteps must be actionable QA recommendations.

            REQUIRED JSON STRUCTURE:
            {
              "riskScore": 75,
              "riskLevel": "HIGH",
              "riskReason": "one sentence summarizing the main risk",
              "topRiskDrivers": ["driver 1", "driver 2", "driver 3"],
              "mitigationSteps": ["action 1", "action 2"],
              "releaseRecommendation": "Block"
            }
            """;

    private final GroqClient groqClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RiskAnalysisStage(GroqClient groqClient) {
        this.groqClient = groqClient;
    }

    public void apply(QaAnalysisResult result, String raw) {
        try {
            String stageInput = buildStageInput(result);
            String groqResponse = groqClient.call(SYSTEM_PROMPT, stageInput);
            applyParsedResponse(result, groqResponse);

        } catch (Exception e) {
            applyFallback(result);
            System.err.println("RiskAnalysisStage LLM error: " + e.getMessage());
        }
    }

    /**
     * Build risk analysis input from ALL previous stage outputs.
     *
     * This is intentional — risk cannot be assessed without context from:
     * - what the feature does (requirements)
     * - what needs to be tested (test cases)
     * - what is still unknown (open questions)
     */
    private String buildStageInput(QaAnalysisResult result) {
        StringBuilder input = new StringBuilder();

        input.append("Feature Summary: ")
             .append(safe(result.getFeatureSummary()))
             .append("\n\n");

        input.append("Requirement Status: ")
             .append(safe(result.getRequirementStatus()))
             .append("\n\n");

        input.append("Clarified Requirements:\n");
        appendList(input, result.getClarifiedRequirements());

        input.append("\nEdge Cases:\n");
        appendList(input, result.getEdgeCases());

        input.append("\nOpen Questions (unresolved):\n");
        appendList(input, result.getOpenQuestions());

        input.append("\nScope:\n");
        appendList(input, result.getScope());

        int testCaseCount = result.getTestCases() != null ? result.getTestCases().size() : 0;
        input.append("\nNumber of test cases generated: ").append(testCaseCount).append("\n");

        return input.toString();
    }

    private void applyParsedResponse(QaAnalysisResult result, String raw) {
        try {
            String cleaned = raw
                    .replaceAll("(?s)```json\\s*", "")
                    .replaceAll("(?s)```\\s*", "")
                    .trim();

            JsonNode node = objectMapper.readTree(cleaned);

            int riskScore = Math.min(node.path("riskScore").asInt(50), 100);
            String riskLevel = node.path("riskLevel").asText("MEDIUM");
            String riskReason = node.path("riskReason").asText("");
            List<String> topRiskDrivers = toStringList(node.path("topRiskDrivers"));
            List<String> mitigationSteps = toStringList(node.path("mitigationSteps"));
            String releaseRecommendation = node.path("releaseRecommendation").asText("Caution");

            result.setRiskScore(riskScore);
            result.setRiskLevel(riskLevel);
            result.setRiskReason(riskReason);
            result.setTopRiskDrivers(topRiskDrivers);
            result.setReleaseRecommendation(releaseRecommendation);

            RiskStageArtifact artifact = new RiskStageArtifact();
            artifact.setRiskScore(riskScore);
            artifact.setRiskLevel(riskLevel);
            artifact.setRiskReason(riskReason);
            artifact.setTopRiskDrivers(topRiskDrivers);
            artifact.setReleaseRecommendation(releaseRecommendation);

            result.setRiskStage(artifact);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Groq risk response: " + e.getMessage(), e);
        }
    }

    private void applyFallback(QaAnalysisResult result) {
        result.setRiskScore(50);
        result.setRiskLevel("MEDIUM");
        result.setRiskReason("Risk analysis unavailable due to LLM error.");
        result.setTopRiskDrivers(List.of("LLM error — manual review required"));
        result.setReleaseRecommendation("Caution");

        RiskStageArtifact artifact = new RiskStageArtifact();
        artifact.setRiskScore(50);
        artifact.setRiskLevel("MEDIUM");
        artifact.setRiskReason("Risk analysis unavailable due to LLM error.");
        artifact.setTopRiskDrivers(List.of("LLM error — manual review required"));
        artifact.setReleaseRecommendation("Caution");
        result.setRiskStage(artifact);
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

    private List<String> toStringList(JsonNode arrayNode) {
        List<String> items = new ArrayList<>();
        if (arrayNode.isArray()) {
            for (JsonNode item : arrayNode) {
                items.add(item.asText());
            }
        }
        return items;
    }
}