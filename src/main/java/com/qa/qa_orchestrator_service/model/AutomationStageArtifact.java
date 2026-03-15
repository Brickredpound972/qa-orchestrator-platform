package com.qa.qa_orchestrator_service.model;

public class AutomationStageArtifact {

    private String automationRecommendation;
    private String automationReasoning;
    private String coverageSplit;
    private String frameworkSuggestion;

    public AutomationStageArtifact() {
    }

    public String getAutomationRecommendation() {
        return automationRecommendation;
    }

    public void setAutomationRecommendation(String automationRecommendation) {
        this.automationRecommendation = automationRecommendation;
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
}