package com.qa.qa_orchestrator_service.service;

import com.qa.qa_orchestrator_service.model.QaAnalysisResult;
import com.qa.qa_orchestrator_service.repository.AnalysisRecord;
import com.qa.qa_orchestrator_service.repository.AnalysisRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * AnalysisRecordService
 *
 * Persists pipeline results to PostgreSQL.
 * Foundation for Phase 5 historical intelligence.
 */
@Service
public class AnalysisRecordService {

    private static final Logger log = LoggerFactory.getLogger(AnalysisRecordService.class);

    private final AnalysisRecordRepository repository;

    public AnalysisRecordService(AnalysisRecordRepository repository) {
        this.repository = repository;
    }

    public void save(QaAnalysisResult result, long pipelineDurationMs) {
        try {
            AnalysisRecord record = new AnalysisRecord();
            record.setIssueKey(result.getTraceabilityId());
            record.setFeatureSummary(result.getFeatureSummary());
            record.setRequirementStatus(result.getRequirementStatus());
            record.setRiskLevel(result.getRiskLevel());
            record.setRiskScore(result.getRiskScore());
            record.setReleaseRecommendation(result.getReleaseRecommendation());
            record.setAutomationRecommendation(result.getAutomationRecommendation());
            record.setTestCaseCount(
                result.getTestCases() != null ? result.getTestCases().size() : 0
            );
            record.setAnalyzedAt(Instant.now());
            record.setPipelineDurationMs(pipelineDurationMs);

            repository.save(record);
            log.info("[DB] Analysis record saved for {} | risk={} | score={} | testCases={}",
                    result.getTraceabilityId(), result.getRiskLevel(),
                    result.getRiskScore(), record.getTestCaseCount());

        } catch (Exception e) {
            log.warn("[DB] Failed to save analysis record for {}: {}",
                    result.getTraceabilityId(), e.getMessage());
        }
    }

    public List<AnalysisRecord> getRecentAnalyses() {
        return repository.findTop10ByOrderByAnalyzedAtDesc();
    }

    public List<AnalysisRecord> getHistoryForIssue(String issueKey) {
        return repository.findByIssueKeyOrderByAnalyzedAtDesc(issueKey);
    }
}