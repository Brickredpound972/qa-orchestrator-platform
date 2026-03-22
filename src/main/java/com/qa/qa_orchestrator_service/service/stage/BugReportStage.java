package com.qa.qa_orchestrator_service.service.stage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qa.qa_orchestrator_service.service.llm.LlmClient;
import com.qa.qa_orchestrator_service.model.QaAnalysisResult;
import com.qa.qa_orchestrator_service.model.BugReportStageArtifact;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BugReportStage {

    private static final String SYSTEM_PROMPT = """
            You are a senior QA engineer creating a bug report template for a software feature.
            You will receive full pipeline context. Your job is to create a pre-filled bug report template as JSON.
            RULES:
            - Return ONLY valid JSON. No markdown, no explanation, no code blocks.
            - severity must be one of: Critical, High, Medium, Low
            - priority must be one of: P1, P2, P3, P4
            - actualResult should be "To be filled by QA engineer after test execution."
            REQUIRED JSON STRUCTURE:
            {
              "title": "bug report title",
              "environment": "QA / Staging",
              "severity": "Critical",
              "priority": "P1",
              "reproductionSteps": ["step 1", "step 2"],
              "expectedResult": "what should happen",
              "actualResult": "To be filled by QA engineer after test execution.",
              "impactSummary": "business impact if this bug exists",
              "affectedAreas": ["area 1", "area 2"],
              "suggestedAssignee": "Backend Developer"
            }
            """;

    private final LlmClient llmClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BugReportStage(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    public void apply(QaAnalysisResult result, String jiraIssueJson) {
        try {
            String context = buildContext(result);
            String response = llmClient.call(SYSTEM_PROMPT, context);
            BugReportStageArtifact artifact = parseResponse(response);
            result.setBugReportStage(artifact);
        } catch (Exception e) {
            BugReportStageArtifact fallback = new BugReportStageArtifact();
            fallback.setTitle("Bug report generation failed");
            fallback.setEnvironment("QA / Staging");
            fallback.setSeverity("Medium");
            fallback.setPriority("P3");
            fallback.setReproductionSteps(List.of());
            fallback.setExpectedResult("");
            fallback.setActualResult("To be filled by QA engineer after test execution.");
            fallback.setImpactSummary("LLM error: " + e.getMessage());
            fallback.setAffectedAreas(List.of());
            fallback.setSuggestedAssignee("Backend Developer");
            result.setBugReportStage(fallback);
        }
    }

    private String buildContext(QaAnalysisResult result) {
        return "Feature: " + result.getFeatureSummary() + "\n"
                + "Risk Level: " + result.getRiskLevel() + "\n"
                + "Risk Score: " + result.getRiskScore() + "\n"
                + "Risk Reason: " + result.getRiskReason() + "\n"
                + "Top Risk Drivers: " + result.getTopRiskDrivers() + "\n"
                + "Scope: " + result.getScope() + "\n"
                + "Automation: " + result.getAutomationRecommendation();
    }

    private BugReportStageArtifact parseResponse(String raw) {
        try {
            String cleaned = raw.replaceAll("(?s)```json\\s*", "").replaceAll("(?s)```\\s*", "").trim();
            JsonNode node = objectMapper.readTree(cleaned);
            BugReportStageArtifact artifact = new BugReportStageArtifact();
            artifact.setTitle(node.path("title").asText(""));
            artifact.setEnvironment(node.path("environment").asText("QA / Staging"));
            artifact.setSeverity(node.path("severity").asText("Medium"));
            artifact.setPriority(node.path("priority").asText("P3"));
            artifact.setReproductionSteps(toStringList(node.path("reproductionSteps")));
            artifact.setExpectedResult(node.path("expectedResult").asText(""));
            artifact.setActualResult(node.path("actualResult").asText("To be filled by QA engineer after test execution."));
            artifact.setImpactSummary(node.path("impactSummary").asText(""));
            artifact.setAffectedAreas(toStringList(node.path("affectedAreas")));
            artifact.setSuggestedAssignee(node.path("suggestedAssignee").asText("Backend Developer"));
            return artifact;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse bug report response: " + e.getMessage(), e);
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