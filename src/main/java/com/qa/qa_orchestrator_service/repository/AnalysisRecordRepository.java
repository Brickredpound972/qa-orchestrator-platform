package com.qa.qa_orchestrator_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * AnalysisRecordRepository
 *
 * Spring Data JPA repository for analysis history.
 */
@Repository
public interface AnalysisRecordRepository extends JpaRepository<AnalysisRecord, Long> {

    List<AnalysisRecord> findByIssueKeyOrderByAnalyzedAtDesc(String issueKey);

    List<AnalysisRecord> findTop10ByOrderByAnalyzedAtDesc();
}