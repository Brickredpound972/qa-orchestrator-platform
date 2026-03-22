package com.qa.qa_orchestrator_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalysisRecordRepository extends JpaRepository<AnalysisRecord, Long> {

    List<AnalysisRecord> findByIssueKeyOrderByAnalyzedAtDesc(String issueKey);

    List<AnalysisRecord> findTop10ByOrderByAnalyzedAtDesc();

    List<AnalysisRecord> findByRiskLevelOrderByRiskScoreDesc(String riskLevel);

    List<AnalysisRecord> findByReleaseRecommendationOrderByAnalyzedAtDesc(String releaseRecommendation);

    @Query("SELECT AVG(a.riskScore) FROM AnalysisRecord a")
    Double findAverageRiskScore();

    @Query("SELECT a.issueKey, COUNT(a) as cnt FROM AnalysisRecord a GROUP BY a.issueKey ORDER BY cnt DESC")
    List<Object[]> findMostAnalyzedIssues();

    @Query("SELECT a.riskLevel, COUNT(a) FROM AnalysisRecord a GROUP BY a.riskLevel")
    List<Object[]> countByRiskLevel();

    @Query("SELECT a.releaseRecommendation, COUNT(a) FROM AnalysisRecord a GROUP BY a.releaseRecommendation")
    List<Object[]> countByReleaseRecommendation();

    long count();
}