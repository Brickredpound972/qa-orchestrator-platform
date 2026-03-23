package com.qa.qa_orchestrator_service.controller;

import com.qa.qa_orchestrator_service.repository.AnalysisRecord;
import com.qa.qa_orchestrator_service.repository.AnalysisRecordRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TrendController — Phase 10
 *
 * Risk trend analysis endpoints.
 * Shows how risk evolves over time per ticket and across the project.
 */
@RestController
@RequestMapping("/qa/api/v1/intelligence")
public class TrendController {

    private final AnalysisRecordRepository repository;

    public TrendController(AnalysisRecordRepository repository) {
        this.repository = repository;
    }

    // Risk trend per issue — shows avg/min/max risk across re-analyses
    @GetMapping("/trends")
    public ResponseEntity<List<Map<String, Object>>> getRiskTrends() {
        List<Object[]> rows = repository.findRiskTrendPerIssue();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] row : rows) {
            Map<String, Object> trend = new HashMap<>();
            trend.put("issueKey", row[0]);
            trend.put("avgRiskScore", row[1] != null ? Math.round(((Number) row[1]).doubleValue()) : 0);
            trend.put("minRiskScore", row[2]);
            trend.put("maxRiskScore", row[3]);
            trend.put("analysisCount", row[4]);

            long min = ((Number) row[2]).longValue();
            long max = ((Number) row[3]).longValue();
            long diff = max - min;

            if (diff > 10) {
                trend.put("trend", "INCREASING");
            } else if (diff < -10) {
                trend.put("trend", "DECREASING");
            } else {
                trend.put("trend", "STABLE");
            }

            result.add(trend);
        }

        return ResponseEntity.ok(result);
    }

    // Full timeline for a specific ticket
    @GetMapping("/trends/{issueKey}")
    public ResponseEntity<Map<String, Object>> getIssueTrend(@PathVariable String issueKey) {
        List<AnalysisRecord> history = repository.findByIssueKeyOrderByAnalyzedAtAsc(issueKey.toUpperCase());

        if (history.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                "issueKey", issueKey,
                "message", "No analysis history found for this issue.",
                "timeline", List.of()
            ));
        }

        List<Map<String, Object>> timeline = history.stream().map(r -> {
            Map<String, Object> point = new HashMap<>();
            point.put("analyzedAt", r.getAnalyzedAt());
            point.put("riskScore", r.getRiskScore());
            point.put("riskLevel", r.getRiskLevel());
            point.put("releaseRecommendation", r.getReleaseRecommendation());
            return point;
        }).toList();

        int firstScore = history.get(0).getRiskScore() != null ? history.get(0).getRiskScore() : 0;
        int lastScore = history.get(history.size() - 1).getRiskScore() != null ? history.get(history.size() - 1).getRiskScore() : 0;
        String direction = lastScore > firstScore + 5 ? "INCREASING" : lastScore < firstScore - 5 ? "DECREASING" : "STABLE";

        Map<String, Object> result = new HashMap<>();
        result.put("issueKey", issueKey.toUpperCase());
        result.put("featureSummary", history.get(0).getFeatureSummary());
        result.put("analysisCount", history.size());
        result.put("firstRiskScore", firstScore);
        result.put("latestRiskScore", lastScore);
        result.put("trend", direction);
        result.put("timeline", timeline);

        return ResponseEntity.ok(result);
    }

    // Most re-analyzed issues — tickets analyzed more than once
    @GetMapping("/reanalyzed")
    public ResponseEntity<List<Map<String, Object>>> getReanalyzedIssues() {
        List<Object[]> rows = repository.findReanalyzedIssues();
        List<Map<String, Object>> result = rows.stream().map(row ->
            Map.<String, Object>of("issueKey", row[0], "analysisCount", row[1])
        ).toList();
        return ResponseEntity.ok(result);
    }
}