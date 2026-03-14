package com.qa.qa_orchestrator_service.model;

import java.util.List;

public class QaAnalysisResult {

    private String traceabilityId;
    private String requirementStatus;
    private String featureSummary;
    private String automationRecommendation;
    private String riskLevel;
    private Integer riskScore;
    private String releaseRecommendation;
    private List<String> clarifiedRequirements;
    private List<String> edgeCases;
    private List<String> openQuestions;
    private List<String> scope;
    private List<String> outOfScope;
    private List<String> testScenarios;
    private List<QaTestCase> testCases;
    private String rawOutput;

    public QaAnalysisResult() {
    }

    public String getTraceabilityId() {
        return traceabilityId;
    }

    public void setTraceabilityId(String traceabilityId) {
        this.traceabilityId = traceabilityId;
    }

    public String getRequirementStatus() {
        return requirementStatus;
    }

    public void setRequirementStatus(String requirementStatus) {
        this.requirementStatus = requirementStatus;
    }

    public String getFeatureSummary() {
        return featureSummary;
    }

    public void setFeatureSummary(String featureSummary) {
        this.featureSummary = featureSummary;
    }

    public String getAutomationRecommendation() {
        return automationRecommendation;
    }

    public void setAutomationRecommendation(String automationRecommendation) {
        this.automationRecommendation = automationRecommendation;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public String getReleaseRecommendation() {
        return releaseRecommendation;
    }

    public void setReleaseRecommendation(String releaseRecommendation) {
        this.releaseRecommendation = releaseRecommendation;
    }

    public List<String> getClarifiedRequirements() {
        return clarifiedRequirements;
    }

    public void setClarifiedRequirements(List<String> clarifiedRequirements) {
        this.clarifiedRequirements = clarifiedRequirements;
    }

    public List<String> getEdgeCases() {
        return edgeCases;
    }

    public void setEdgeCases(List<String> edgeCases) {
        this.edgeCases = edgeCases;
    }

    public List<String> getOpenQuestions() {
        return openQuestions;
    }

    public void setOpenQuestions(List<String> openQuestions) {
        this.openQuestions = openQuestions;
    }

    public List<String> getScope() {
        return scope;
    }

    public void setScope(List<String> scope) {
        this.scope = scope;
    }

    public List<String> getOutOfScope() {
        return outOfScope;
    }

    public void setOutOfScope(List<String> outOfScope) {
        this.outOfScope = outOfScope;
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

    public String getRawOutput() {
        return rawOutput;
    }

    public void setRawOutput(String rawOutput) {
        this.rawOutput = rawOutput;
    }
}