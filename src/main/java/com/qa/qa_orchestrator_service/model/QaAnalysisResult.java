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
    private String riskReason;
    private String rawReleaseRecommendation;
    private java.util.List<String> topRiskDrivers;
    private String automationReasoning;
    private String coverageSplit;
    private String frameworkSuggestion;
    private String analysisSummary;
    private RequirementStageArtifact requirementStage;

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

    public String getRiskReason() {
        return riskReason;
    }

    public void setRiskReason(String riskReason) {
        this.riskReason = riskReason;
    }

    public java.util.List<String> getTopRiskDrivers() {
        return topRiskDrivers;
    }

    public void setTopRiskDrivers(java.util.List<String> topRiskDrivers) {
        this.topRiskDrivers = topRiskDrivers;
    }

    public String getRawReleaseRecommendation() {
        return rawReleaseRecommendation;
    }

    public void setRawReleaseRecommendation(String rawReleaseRecommendation) {
        this.rawReleaseRecommendation = rawReleaseRecommendation;
    }

    public String getAutomationReasoning() {
        return automationReasoning;
    }

    public void setAutomationReasoning(String automationReasoning) {
        this.automationReasoning = automationReasoning;
    }

    public String getCoverageSplit() {
        return coverageSplit;
    }

    public void setCoverageSplit(String coverageSplit) {
        this.coverageSplit = coverageSplit;
    }

    public String getFrameworkSuggestion() {
        return frameworkSuggestion;
    }

    public void setFrameworkSuggestion(String frameworkSuggestion) {
        this.frameworkSuggestion = frameworkSuggestion;
    }

    public String getAnalysisSummary() {
        return analysisSummary;
    }

    public void setAnalysisSummary(String analysisSummary) {
        this.analysisSummary = analysisSummary;
    }

    public RequirementStageArtifact getRequirementStage() {
        return requirementStage;
    }

    public void setRequirementStage(RequirementStageArtifact requirementStage) {
        this.requirementStage = requirementStage;
    }
}