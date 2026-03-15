package com.qa.qa_orchestrator_service.model;

import java.util.List;

public class TestDesignStageArtifact {

    private List<String> testScenarios;
    private List<QaTestCase> testCases;

    public TestDesignStageArtifact() {
    }

    public List<String> getTestScenarios() {
        return testScenarios;
    }

    public void setTestScenarios(List<String> testScenarios) {
        this.testScenarios = testScenarios;
    }

    public List<QaTestCase> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<QaTestCase> testCases) {
        this.testCases = testCases;
    }
}