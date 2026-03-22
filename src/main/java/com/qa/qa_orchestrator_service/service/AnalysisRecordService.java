package com.qa.qa_orchestrator_service.service;

import com.qa.qa_orchestrator_service.model.QaAnalysisResult;
import com.qa.qa_orchestrator_service.repository.AnalysisRecord;
import com.qa.qa_orchestrator_service.repository.AnalysisRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public List<AnalysisRecord> getHighRiskTickets() {
        return repository.findByRiskLevelOrderByRiskScoreDesc("HIGH");
    }

    public List<AnalysisRecord> getBlockedTickets() {
        return repository.findByReleaseRecommendationOrderByAnalyzedAtDesc("Block");
    }

    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new HashMap<>();

        long total = repository.count();
        Double avgRisk = repository.findAverageRiskScore();
        List<AnalysisRecord> highRisk = repository.findByRiskLevelOrderByRiskScoreDesc("HIGH");
        List<AnalysisRecord> blocked = repository.findByReleaseRecommendationOrderByAnalyzedAtDesc("Block");
        List<Object[]> mostAnalyzed = repository.findMostAnalyzedIssues();

        summary.put("totalAnalyses", total);
        summary.put("averageRiskScore", avgRisk != null ? Math.round(avgRisk) : 0);
        summary.put("highRiskCount", highRisk.size());
        summary.put("blockedCount", blocked.size());
        summary.put("mostAnalyzedIssues", mostAnalyzed.stream()
                .limit(5)
                .map(row -> Map.of("issueKey", row[0], "count", row[1]))
                .toList());

        return summary;
    }
}