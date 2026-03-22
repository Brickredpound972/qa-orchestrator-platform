package com.qa.qa_orchestrator_service.repository;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "analysis_records")
public class AnalysisRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "issue_key", nullable = false)
    private String issueKey;

    @Column(name = "feature_summary", length = 1000)
    private String featureSummary;

    @Column(name = "requirement_status")
    private String requirementStatus;

    @Column(name = "risk_level")
    private String riskLevel;

    @Column(name = "risk_score")
    private Integer riskScore;

    @Column(name = "release_recommendation")
    private String releaseRecommendation;

    @Column(name = "automation_recommendation")
    private String automationRecommendation;

    @Column(name = "test_case_count")
    private Integer testCaseCount;

    @Column(name = "analyzed_at", nullable = false)
    private Instant analyzedAt;

    @Column(name = "pipeline_duration_ms")
    private Long pipelineDurationMs;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "release_summary", length = 5000)
    private String releaseSummary;

    public AnalysisRecord() {}

    public Long getId() { return id; }
    public String getIssueKey() { return issueKey; }
    public void setIssueKey(String issueKey) { this.issueKey = issueKey; }
    public String getFeatureSummary() { return featureSummary; }
    public void setFeatureSummary(String featureSummary) { this.featureSummary = featureSummary; }
    public String getRequirementStatus() { return requirementStatus; }
    public void setRequirementStatus(String requirementStatus) { this.requirementStatus = requirementStatus; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public Integer getRiskScore() { return riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }
    public String getReleaseRecommendation() { return releaseRecommendation; }
    public void setReleaseRecommendation(String releaseRecommendation) { this.releaseRecommendation = releaseRecommendation; }
    public String getAutomationRecommendation() { return automationRecommendation; }
    public void setAutomationRecommendation(String automationRecommendation) { this.automationRecommendation = automationRecommendation; }
    public Integer getTestCaseCount() { return testCaseCount; }
    public void setTestCaseCount(Integer testCaseCount) { this.testCaseCount = testCaseCount; }
    public Instant getAnalyzedAt() { return analyzedAt; }
    public void setAnalyzedAt(Instant analyzedAt) { this.analyzedAt = analyzedAt; }
    public Long getPipelineDurationMs() { return pipelineDurationMs; }
    public void setPipelineDurationMs(Long pipelineDurationMs) { this.pipelineDurationMs = pipelineDurationMs; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public String getReleaseSummary() { return releaseSummary; }
    public void setReleaseSummary(String releaseSummary) { this.releaseSummary = releaseSummary; }
}