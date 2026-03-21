package com.qa.qa_orchestrator_service.service.stage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qa.qa_orchestrator_service.service.llm.GroqClient;
import com.qa.qa_orchestrator_service.model.BugReportStageArtifact;
import com.qa.qa_orchestrator_service.model.QaAnalysisResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * BugReportStage — LLM-powered
 *
 * Generates a structured bug report template based on the full pipeline context.
 *
 * KEY DESIGN DECISION:
 * This stage always runs — it generates a TEMPLATE, not an actual bug report.
 * The template is pre-filled based on what we know from the analysis:
 * - High-risk areas become the affected areas
 * - Open questions become potential failure points
 * - Test cases become reproduction step candidates
 *
 * When an actual bug is found during testing, the QA engineer uses this
 * template as a starting point — they fill in actualResult and evidence.
 *
 * This feeds the Copilot Studio agent_bug_reporter topic.
 */
@Component
public class BugReportStage {

    private static final String SYSTEM_PROMPT = """
            You are a senior QA engineer generating a structured bug report template.

            You will receive a QA analysis summary including requirements, risk drivers,
            test cases, and open questions. Your job is to generate a pre-filled bug report
            template that a QA engineer can use when a defect is found during testing.

            RULES:
            - Return ONLY valid JSON. No markdown, no explanation, no code blocks.
            - title must be a clear, specific bug report title based on the feature.
            - severity must be one of: Critical, High, Medium, Low
            - priority must be one of: P1, P2, P3, P4
            - Map severity to priority: Critical=P1, High=P2, Medium=P3, Low=P4
            - reproductionSteps must be concrete, numbered steps a developer can follow.
            - expectedResult must describe what SHOULD happen.
            - actualResult must say "To be filled by QA engineer after test execution."
            - affectedAreas must list the system components at risk based on the analysis.
            - suggestedAssignee must be a role, not a person name (e.g., "Backend Developer", "Frontend Developer").
            - Base severity on the risk level provided.

            SEVERITY MAPPING:
            HIGH risk   → Critical or High severity
            MEDIUM risk → Medium severity
            LOW risk    → Low severity

            REQUIRED JSON STRUCTURE:
            {
              "title": "specific bug report title",
              "environment": "QA / Staging",
              "severity": "High",
              "priority": "P2",
              "reproductionSteps": ["step 1", "step 2", "step 3"],
              "expectedResult": "what should happen",
              "actualResult": "To be filled by QA engineer after test execution.",
              "impactSummary": "one sentence describing the business impact if this bug exists",
              "affectedAreas": ["component 1", "component 2"],
              "suggestedAssignee": "role name"
            }
            """;

    private final GroqClient groqClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BugReportStage(GroqClient groqClient) {
        this.groqClient = groqClient;
    }

    public void apply(QaAnalysisResult result, String jiraJson) {
        try {
            String stageInput = buildStageInput(result);
            String groqResponse = groqClient.call(SYSTEM_PROMPT, stageInput);
            BugReportStageArtifact artifact = parseResponse(groqResponse);
            result.setBugReportStage(artifact);

        } catch (Exception e) {
            result.setBugReportStage(buildFallbackArtifact(result));
            System.err.println("BugReportStage LLM error: " + e.getMessage());
        }
    }

    /**
     * Build input from full pipeline context.
     *
     * Bug report template quality depends on:
     * - Feature summary (what is being tested)
     * - Risk level and drivers (severity mapping)
     * - Top test cases (reproduction steps candidates)
     * - Open questions (potential unknown failure areas)
     * - Affected scope (system components)
     */
    private String buildStageInput(QaAnalysisResult result) {
        StringBuilder input = new StringBuilder();

        input.append("Feature Summary: ")
             .append(safe(result.getFeatureSummary()))
             .append("\n\n");

        input.append("Risk Level: ").append(safe(result.getRiskLevel())).append("\n");
        input.append("Risk Score: ").append(result.getRiskScore() != null ? result.getRiskScore() : "N/A").append("\n");
        input.append("Risk Reason: ").append(safe(result.getRiskReason())).append("\n\n");

        input.append("Top Risk Drivers:\n");
        appendList(input, result.getTopRiskDrivers());

        input.append("\nScope (affected system areas):\n");
        appendList(input, result.getScope());

        input.append("\nKey Test Cases (use as reproduction step candidates):\n");
        if (result.getTestCases() != null && !result.getTestCases().isEmpty()) {
            int limit = Math.min(result.getTestCases().size(), 5);
            for (int i = 0; i < limit; i++) {
                var tc = result.getTestCases().get(i);
                input.append("- ").append(tc.getTitle()).append(": ").append(tc.getExpectedResult()).append("\n");
            }
        }

        input.append("\nOpen Questions (potential unknown failure areas):\n");
        appendList(input, result.getOpenQuestions());

        input.append("\nRelease Recommendation: ").append(safe(result.getReleaseRecommendation())).append("\n");

        return input.toString();
    }

    private BugReportStageArtifact parseResponse(String raw) {
        try {
            String cleaned = raw
                    .replaceAll("(?s)```json\\s*", "")
                    .replaceAll("(?s)```\\s*", "")
                    .trim();

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
            artifact.setSuggestedAssignee(node.path("suggestedAssignee").asText(""));

            return artifact;

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Groq bug report response: " + e.getMessage(), e);
        }
    }

    private BugReportStageArtifact buildFallbackArtifact(QaAnalysisResult result) {
        BugReportStageArtifact artifact = new BugReportStageArtifact();
        artifact.setTitle("Bug report template unavailable — manual creation required");
        artifact.setEnvironment("QA / Staging");
        artifact.setSeverity("Medium");
        artifact.setPriority("P3");
        artifact.setReproductionSteps(List.of("To be filled by QA engineer"));
        artifact.setExpectedResult("To be filled by QA engineer");
        artifact.setActualResult("To be filled by QA engineer after test execution.");
        artifact.setImpactSummary("Impact assessment pending manual review.");
        artifact.setAffectedAreas(result.getScope() != null ? result.getScope() : List.of());
        artifact.setSuggestedAssignee("QA Engineer");
        return artifact;
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