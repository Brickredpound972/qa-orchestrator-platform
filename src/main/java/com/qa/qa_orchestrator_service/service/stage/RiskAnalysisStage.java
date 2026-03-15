package com.qa.qa_orchestrator_service.service.stage;

import com.qa.qa_orchestrator_service.model.QaAnalysisResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RiskAnalysisStage {

    public void apply(QaAnalysisResult result, String raw) {
        String rawRiskLevel = extractSingleValue(raw, "Risk Level:");
        String riskReason = extractSingleValue(raw, "Reason:");
        int riskScore = calculateRiskScore(raw, rawRiskLevel);

        result.setRiskScore(riskScore);
        result.setRiskLevel(mapRiskLevel(riskScore, rawRiskLevel));
        result.setRiskReason(riskReason);
        result.setTopRiskDrivers(buildTopRiskDrivers(raw));
        result.setRawReleaseRecommendation(extractSingleValue(raw, "Release Recommendation:"));
        result.setReleaseRecommendation(mapReleaseRecommendation(riskScore));
    }

    private String extractSingleValue(String raw, String prefix) {
        if (raw == null || prefix == null) {
            return null;
        }

        String[] lines = raw.split("\\R");
        for (String line : lines) {
            if (line.trim().startsWith(prefix)) {
                return line.replace(prefix, "").trim();
            }
        }
        return null;
    }

    private List<String> buildTopRiskDrivers(String raw) {
        List<String> drivers = new ArrayList<>();

        String reason = extractSingleValue(raw, "Reason:");
        if (reason != null && !reason.isBlank()) {
            String[] parts = reason.split(",");
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    drivers.add(trimmed);
                }
            }
        }

        return drivers;
    }

    private int calculateRiskScore(String raw, String rawRiskLevel) {
        int score = 0;
        String lower = raw == null ? "" : raw.toLowerCase();

        if (lower.contains("checkout") || lower.contains("payment") || lower.contains("login")) {
            score += 20;
        }

        if (lower.contains("regression")) {
            score += 15;
        }

        if (lower.contains("session persistence") || lower.contains("session") || lower.contains("state")) {
            score += 10;
        }

        if (lower.contains("financial impact")
                || lower.contains("subtotal")
                || lower.contains("tax")
                || lower.contains("discount")
                || lower.contains("coupon")) {
            score += 15;
        }

        if (lower.contains("multiple coupon")
                || lower.contains("restriction")
                || lower.contains("validation")) {
            score += 10;
        }

        if ("HIGH".equalsIgnoreCase(rawRiskLevel) && score < 70) {
            score = 70;
        } else if ("MEDIUM".equalsIgnoreCase(rawRiskLevel) && score < 40) {
            score = 40;
        }

        return Math.min(score, 100);
    }

    private String mapRiskLevel(int riskScore, String fallbackLevel) {
        if (riskScore >= 70) {
            return "HIGH";
        }
        if (riskScore >= 40) {
            return "MEDIUM";
        }
        if (riskScore >= 0) {
            return "LOW";
        }
        return fallbackLevel != null ? fallbackLevel : "UNKNOWN";
    }

    private String mapReleaseRecommendation(int riskScore) {
        if (riskScore >= 70) {
            return "Block";
        }
        if (riskScore >= 40) {
            return "Caution";
        }
        return "Go";
    }
}