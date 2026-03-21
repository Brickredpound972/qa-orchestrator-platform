package com.qa.qa_orchestrator_service.model;

/**
 * QaStagesArtifact
 *
 * The canonical structured output — analysis.stages
 * Each field maps to one pipeline stage artifact.
 */
public class QaStagesArtifact {

    private RequirementStageArtifact requirement;
    private TestDesignStageArtifact testDesign;
    private AutomationStageArtifact automation;
    private RiskStageArtifact risk;
    private BugReportStageArtifact bugReport;

    public QaStagesArtifact() {}

    public RequirementStageArtifact getRequirement() { return requirement; }
    public void setRequirement(RequirementStageArtifact requirement) { this.requirement = requirement; }

    public TestDesignStageArtifact getTestDesign() { return testDesign; }
    public void setTestDesign(TestDesignStageArtifact testDesign) { this.testDesign = testDesign; }

    public AutomationStageArtifact getAutomation() { return automation; }
    public void setAutomation(AutomationStageArtifact automation) { this.automation = automation; }

    public RiskStageArtifact getRisk() { return risk; }
    public void setRisk(RiskStageArtifact risk) { this.risk = risk; }

    public BugReportStageArtifact getBugReport() { return bugReport; }
    public void setBugReport(BugReportStageArtifact bugReport) { this.bugReport = bugReport; }
}