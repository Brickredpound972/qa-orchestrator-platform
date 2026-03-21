package com.qa.qa_orchestrator_service.service.stage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qa.qa_orchestrator_service.service.llm.GroqClient;
import com.qa.qa_orchestrator_service.model.QaAnalysisResult;
import com.qa.qa_orchestrator_service.model.RequirementStageArtifact;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * RequirementAnalysisStage — LLM-powered (Groq)
 *
 * Replaces keyword matching with real AI reasoning.
 */
@Component
public class RequirementAnalysisStage {

    private static final String SYSTEM_PROMPT = """
            You are a senior QA engineer performing requirement analysis on a Jira issue.

            Your job is to analyze the provided Jira issue and return a structured JSON object.

            RULES:
            - Return ONLY valid JSON. No markdown, no explanation, no code blocks.
            - If the issue lacks enough detail to test (missing acceptance criteria, missing feature behavior,
              missing scope), set status to "BLOCKED".
            - If the issue is sufficiently clear, set status to "READY".
            - Be specific and concrete. Do not invent requirements not present in the issue.
            - edgeCases must include boundary conditions, invalid inputs, and state-related edge cases.
            - openQuestions must be real gaps that would block test execution.

            REQUIRED JSON STRUCTURE:
            {
              "status": "READY" or "BLOCKED",
              "featureSummary": "one sentence description of what is being tested",
              "clarifiedRequirements": ["requirement 1", "requirement 2"],
              "edgeCases": ["edge case 1", "edge case 2"],
              "openQuestions": ["question 1", "question 2"],
              "scope": ["in-scope item 1", "in-scope item 2"],
              "outOfScope": ["out-of-scope item 1", "out-of-scope item 2"]
            }
            """;

    private final GroqClient groqClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RequirementAnalysisStage(GroqClient groqClient) {
        this.groqClient = groqClient;
    }

    public void apply(QaAnalysisResult result, String jiraIssueJson) {
        try {
            String groqResponse = groqClient.call(SYSTEM_PROMPT, jiraIssueJson);
            RequirementStageArtifact artifact = parseResponse(groqResponse);
            applyToResult(result, artifact);

        } catch (Exception e) {
            RequirementStageArtifact fallback = buildFallbackArtifact(e.getMessage());
            applyToResult(result, fallback);
        }
    }

    private RequirementStageArtifact parseResponse(String raw) {
        try {
            String cleaned = raw
                    .replaceAll("(?s)```json\\s*", "")
                    .replaceAll("(?s)```\\s*", "")
                    .trim();

            JsonNode node = objectMapper.readTree(cleaned);

            RequirementStageArtifact artifact = new RequirementStageArtifact();
            artifact.setStatus(node.path("status").asText("UNKNOWN"));
            artifact.setFeatureSummary(node.path("featureSummary").asText(""));
            artifact.setClarifiedRequirements(toStringList(node.path("clarifiedRequirements")));
            artifact.setEdgeCases(toStringList(node.path("edgeCases")));
            artifact.setOpenQuestions(toStringList(node.path("openQuestions")));
            artifact.setScope(toStringList(node.path("scope")));
            artifact.setOutOfScope(toStringList(node.path("outOfScope")));

            return artifact;

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Groq requirement response: " + e.getMessage(), e);
        }
    }

    private void applyToResult(QaAnalysisResult result, RequirementStageArtifact artifact) {
        result.setRequirementStatus(artifact.getStatus());
        result.setFeatureSummary(artifact.getFeatureSummary());
        result.setClarifiedRequirements(artifact.getClarifiedRequirements());
        result.setEdgeCases(artifact.getEdgeCases());
        result.setOpenQuestions(artifact.getOpenQuestions());
        result.setScope(artifact.getScope());
        result.setOutOfScope(artifact.getOutOfScope());
        result.setRequirementStage(artifact);
    }

    private RequirementStageArtifact buildFallbackArtifact(String errorMessage) {
        RequirementStageArtifact artifact = new RequirementStageArtifact();
        artifact.setStatus("BLOCKED");
        artifact.setFeatureSummary("Requirement analysis failed due to LLM error.");
        artifact.setClarifiedRequirements(List.of());
        artifact.setEdgeCases(List.of());
        artifact.setOpenQuestions(List.of("LLM call failed: " + errorMessage));
        artifact.setScope(List.of());
        artifact.setOutOfScope(List.of());
        return artifact;
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