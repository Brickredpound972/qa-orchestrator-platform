package com.qa.qa_orchestrator_service.model;

import java.util.List;

/**
 * BugReportStageArtifact
 *
 * Represents a structured bug report template generated from QA analysis.
 * This is populated by BugReportStage and exposed under analysis.stages.bugReport
 *
 * Consumed by:
 * - Copilot Studio agent_bug_reporter topic
 * - Any external bug tracking integration
 */
public class BugReportStageArtifact {

    private String title;
    private String environment;
    private String severity;
    private String priority;
    private List<String> reproductionSteps;
    private String expectedResult;
    private String actualResult;
    private String impactSummary;
    private List<String> affectedAreas;
    private String suggestedAssignee;

    public BugReportStageArtifact() {
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public List<String> getReproductionSteps() { return reproductionSteps; }
    public void setReproductionSteps(List<String> reproductionSteps) { this.reproductionSteps = reproductionSteps; }

    public String getExpectedResult() { return expectedResult; }
    public void setExpectedResult(String expectedResult) { this.expectedResult = expectedResult; }

    public String getActualResult() { return actualResult; }
    public void setActualResult(String actualResult) { this.actualResult = actualResult; }

    public String getImpactSummary() { return impactSummary; }
    public void setImpactSummary(String impactSummary) { this.impactSummary = impactSummary; }

    public List<String> getAffectedAreas() { return affectedAreas; }
    public void setAffectedAreas(List<String> affectedAreas) { this.affectedAreas = affectedAreas; }

    public String getSuggestedAssignee() { return suggestedAssignee; }
    public void setSuggestedAssignee(String suggestedAssignee) { this.suggestedAssignee = suggestedAssignee; }
}