package com.qa.qa_orchestrator_service.service.stage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qa.qa_orchestrator_service.service.llm.LlmClient;
import com.qa.qa_orchestrator_service.model.QaAnalysisResult;
import com.qa.qa_orchestrator_service.model.QaTestCase;
import com.qa.qa_orchestrator_service.model.TestDesignStageArtifact;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TestDesignStage {

    private static final String SYSTEM_PROMPT = """
            You are a senior QA engineer designing test cases for a software feature.
            You will receive requirement analysis output from a previous stage.
            Your job is to produce structured test scenarios and test cases as JSON.
            RULES:
            - Return ONLY valid JSON. No markdown, no explanation, no code blocks.
            - Generate realistic, executable test cases based on the requirements and edge cases.
            - Each test case must have a unique id (TC-01, TC-02, etc.).
            - testType must be one of: UI, API, E2E
            - suiteTag must be one of: Smoke, Regression
            - priority must be one of: High, Medium, Low
            REQUIRED JSON STRUCTURE:
            {
              "testScenarios": ["scenario 1", "scenario 2"],
              "testCases": [
                {
                  "id": "TC-01",
                  "title": "test case title",
                  "preconditions": "preconditions",
                  "steps": ["step 1", "step 2"],
                  "expectedResult": "expected result",
                  "testType": "UI",
                  "suiteTag": "Smoke",
                  "testData": "test data",
                  "priority": "High"
                }
              ]
            }
            """;

    private final LlmClient llmClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TestDesignStage(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    public void apply(QaAnalysisResult result, String jiraIssueJson) {
        try {
            String context = buildContext(result);
            String response = llmClient.call(SYSTEM_PROMPT, context);
            TestDesignStageArtifact artifact = parseResponse(response);
            result.setTestScenarios(artifact.getTestScenarios());
            result.setTestCases(artifact.getTestCases());
            result.setTestDesignStage(artifact);
        } catch (Exception e) {
            TestDesignStageArtifact fallback = new TestDesignStageArtifact();
            fallback.setTestScenarios(List.of());
            fallback.setTestCases(List.of());
            result.setTestDesignStage(fallback);
        }
    }

    private String buildContext(QaAnalysisResult result) {
        return "Feature: " + result.getFeatureSummary() + "\n"
                + "Requirements: " + result.getClarifiedRequirements() + "\n"
                + "Edge Cases: " + result.getEdgeCases() + "\n"
                + "Scope: " + result.getScope();
    }

    private TestDesignStageArtifact parseResponse(String raw) {
        try {
            String cleaned = raw.replaceAll("(?s)```json\\s*", "").replaceAll("(?s)```\\s*", "").trim();
            JsonNode node = objectMapper.readTree(cleaned);
            TestDesignStageArtifact artifact = new TestDesignStageArtifact();
            artifact.setTestScenarios(toStringList(node.path("testScenarios")));
            artifact.setTestCases(toTestCaseList(node.path("testCases")));
            return artifact;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse test design response: " + e.getMessage(), e);
        }
    }

    private List<String> toStringList(JsonNode arrayNode) {
        List<String> items = new ArrayList<>();
        if (arrayNode.isArray()) {
            for (JsonNode item : arrayNode) items.add(item.asText());
        }
        return items;
    }

    private List<QaTestCase> toTestCaseList(JsonNode arrayNode) {
        List<QaTestCase> items = new ArrayList<>();
        if (arrayNode.isArray()) {
            for (JsonNode node : arrayNode) {
                QaTestCase tc = new QaTestCase();
                tc.setId(node.path("id").asText(""));
                tc.setTitle(node.path("title").asText(""));
                tc.setPreconditions(node.path("preconditions").asText(""));
                tc.setSteps(toStringList(node.path("steps")));
                tc.setExpectedResult(node.path("expectedResult").asText(""));
                tc.setTestType(node.path("testType").asText("UI"));
                tc.setSuiteTag(node.path("suiteTag").asText("Regression"));
                tc.setTestData(node.path("testData").asText(""));
                tc.setPriority(node.path("priority").asText("Medium"));
                items.add(tc);
            }
        }
        return items;
    }
}