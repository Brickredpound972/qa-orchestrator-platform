package com.qa.qa_orchestrator_service.model;

import java.util.List;

public class RiskStageArtifact {

    private Integer riskScore;
    private String riskLevel;
    private String riskReason;
    private List<String> topRiskDrivers;
    private String rawReleaseRecommendation;
    private String releaseRecommendation;

    public RiskStageArtifact() {
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getRiskReason() {
        return riskReason;
    }

    public void setRiskReason(String riskReason) {
        this.riskReason = riskReason;
    }

    public List<String> getTopRiskDrivers() {
        return topRiskDrivers;
    }

    public void setTopRiskDrivers(List<String> topRiskDrivers) {
        this.topRiskDrivers = topRiskDrivers;
    }

    public String getRawReleaseRecommendation() {
        return rawReleaseRecommendation;
    }

    public void setRawReleaseRecommendation(String rawReleaseRecommendation) {
        this.rawReleaseRecommendation = rawReleaseRecommendation;
    }

    public String getReleaseRecommendation() {
        return releaseRecommendation;
    }

    public void setReleaseRecommendation(String releaseRecommendation) {
        this.releaseRecommendation = releaseRecommendation;
    }
}