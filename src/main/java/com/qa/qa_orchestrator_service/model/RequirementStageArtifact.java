package com.qa.qa_orchestrator_service.model;

import java.util.List;

public class RequirementStageArtifact {

    private String status;
    private String featureSummary;
    private List<String> clarifiedRequirements;
    private List<String> edgeCases;
    private List<String> openQuestions;
    private List<String> scope;
    private List<String> outOfScope;

    public RequirementStageArtifact() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFeatureSummary() {
        return featureSummary;
    }

    public void setFeatureSummary(String featureSummary) {
        this.featureSummary = featureSummary;
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
}