package com.qa.qa_orchestrator_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalysisRecordRepository extends JpaRepository<AnalysisRecord, Long> {

    List<AnalysisRecord> findByIssueKeyOrderByAnalyzedAtDesc(String issueKey);

    List<AnalysisRecord> findTop10ByOrderByAnalyzedAtDesc();

    // High risk tickets
    List<AnalysisRecord> findByRiskLevelOrderByRiskScoreDesc(String riskLevel);

    // Block recommendations
    List<AnalysisRecord> findByReleaseRecommendationOrderByAnalyzedAtDesc(String releaseRecommendation);

    // Average risk score across all analyses
    @Query("SELECT AVG(a.riskScore) FROM AnalysisRecord a")
    Double findAverageRiskScore();

    // Most analyzed tickets
    @Query("SELECT a.issueKey, COUNT(a) as cnt FROM AnalysisRecord a GROUP BY a.issueKey ORDER BY cnt DESC")
    List<Object[]> findMostAnalyzedIssues();

    // Total analysis count
    long count();
}