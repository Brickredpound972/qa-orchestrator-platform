package com.qa.qa_orchestrator_service.service.stage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qa.qa_orchestrator_service.service.llm.GroqClient;
import com.qa.qa_orchestrator_service.model.QaAnalysisResult;
import com.qa.qa_orchestrator_service.model.QaTestCase;
import com.qa.qa_orchestrator_service.model.TestDesignStageArtifact;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * TestDesignStage — LLM-powered
 *
 * Takes RequirementStage output (clarifiedRequirements + edgeCases)
 * and produces concrete, execution-ready test cases via Groq.
 *
 * KEY LESSON:
 * This stage does NOT re-read the raw Jira JSON.
 * It reads the OUTPUT of RequirementAnalysisStage.
 * This is what makes it a real pipeline — stages feed each other.
 */
@Component
public class TestDesignStage {

    private static final String SYSTEM_PROMPT = """
            You are a senior QA engineer designing test cases.

            You will receive a list of clarified requirements and edge cases extracted from a Jira issue.
            Your job is to produce structured, execution-ready test cases.

            RULES:
            - Return ONLY valid JSON. No markdown, no explanation, no code blocks.
            - Generate at least 5 test cases.
            - Cover both positive (happy path) and negative (failure/validation) scenarios.
            - Cover edge cases where relevant.
            - testType must be one of: UI, API, E2E
            - suiteTag must be one of: Smoke, Regression
            - priority must be one of: High, Medium, Low
            - steps must be a concrete list of user or system actions.
            - expectedResult must be a specific, verifiable outcome.

            REQUIRED JSON STRUCTURE:
            {
              "testScenarios": ["scenario 1", "scenario 2"],
              "testCases": [
                {
                  "id": "TC-01",
                  "title": "short descriptive title",
                  "preconditions": "what must be true before this test runs",
                  "steps": ["step 1", "step 2", "step 3"],
                  "expectedResult": "what should happen if the feature works correctly",
                  "testType": "UI",
                  "suiteTag": "Smoke",
                  "testData": "specific data used in this test",
                  "priority": "High"
                }
              ]
            }
            """;

    private final GroqClient groqClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TestDesignStage(GroqClient groqClient) {
        this.groqClient = groqClient;
    }

    public void apply(QaAnalysisResult result, String raw) {
        try {
            // Build input from RequirementStage output — not from raw Jira JSON
            String stageInput = buildStageInput(result);
            String groqResponse = groqClient.call(SYSTEM_PROMPT, stageInput);
            applyParsedResponse(result, groqResponse);

        } catch (Exception e) {
            applyFallback(result, e.getMessage());
        }
    }

    /**
     * Build the prompt input using RequirementStage output.
     *
     * This is the key architectural point:
     * We pass clarifiedRequirements + edgeCases to the LLM,
     * not the raw Jira ticket. Each stage builds on the previous one.
     */
    private String buildStageInput(QaAnalysisResult result) {
        StringBuilder input = new StringBuilder();

        input.append("Feature Summary: ")
             .append(result.getFeatureSummary() != null ? result.getFeatureSummary() : "Not available")
             .append("\n\n");

        input.append("Clarified Requirements:\n");
        List<String> requirements = result.getClarifiedRequirements();
        if (requirements != null && !requirements.isEmpty()) {
            for (String req : requirements) {
                input.append("- ").append(req).append("\n");
            }
        } else {
            input.append("- Not available\n");
        }

        input.append("\nEdge Cases:\n");
        List<String> edgeCases = result.getEdgeCases();
        if (edgeCases != null && !edgeCases.isEmpty()) {
            for (String edge : edgeCases) {
                input.append("- ").append(edge).append("\n");
            }
        } else {
            input.append("- Not available\n");
        }

        return input.toString();
    }

    private void applyParsedResponse(QaAnalysisResult result, String raw) {
        try {
            String cleaned = raw
                    .replaceAll("(?s)```json\\s*", "")
                    .replaceAll("(?s)```\\s*", "")
                    .trim();

            JsonNode node = objectMapper.readTree(cleaned);

            List<String> testScenarios = toStringList(node.path("testScenarios"));
            List<QaTestCase> testCases = parseTestCases(node.path("testCases"));

            result.setTestScenarios(testScenarios);
            result.setTestCases(testCases);

            TestDesignStageArtifact artifact = new TestDesignStageArtifact();
            artifact.setTestScenarios(testScenarios);
            artifact.setTestCases(testCases);
            result.setTestDesignStage(artifact);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Groq test design response: " + e.getMessage(), e);
        }
    }

    private List<QaTestCase> parseTestCases(JsonNode testCasesNode) {
        List<QaTestCase> testCases = new ArrayList<>();

        if (!testCasesNode.isArray()) {
            return testCases;
        }

        for (JsonNode tc : testCasesNode) {
            QaTestCase testCase = new QaTestCase();
            testCase.setId(tc.path("id").asText("TC-00"));
            testCase.setTitle(tc.path("title").asText(""));
            testCase.setPreconditions(tc.path("preconditions").asText(""));
            testCase.setSteps(toStringList(tc.path("steps")));
            testCase.setExpectedResult(tc.path("expectedResult").asText(""));
            testCase.setTestType(tc.path("testType").asText("UI"));
            testCase.setSuiteTag(tc.path("suiteTag").asText("Regression"));
            testCase.setTestData(tc.path("testData").asText(""));
            testCase.setPriority(tc.path("priority").asText("Medium"));
            testCases.add(testCase);
        }

        return testCases;
    }

    private void applyFallback(QaAnalysisResult result, String errorMessage) {
        result.setTestScenarios(List.of());
        result.setTestCases(List.of());

        TestDesignStageArtifact artifact = new TestDesignStageArtifact();
        artifact.setTestScenarios(List.of());
        artifact.setTestCases(List.of());
        result.setTestDesignStage(artifact);

        System.err.println("TestDesignStage LLM error: " + errorMessage);
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