package com.qa.qa_orchestrator_service.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class QaAnalyzeRequest {

    @NotBlank(message = "issueKey is required")
    @Size(max = 50, message = "issueKey must be 50 characters or less")
    private String issueKey;

    public String getIssueKey() {
        return issueKey;
    }

    public void setIssueKey(String issueKey) {
        if (issueKey != null) {
            this.issueKey = normalize(issueKey.trim());
        }
    }

    /**
     * Normalizes issueKey to Jira format.
     * Examples:
     *   "project-5"   → "PROJECT-5"
     *   "proj 5"      → "PROJ-5"
     *   "PROJ-5"      → "PROJ-5"
     *   "proj5"       → "PROJ-5"  (if digits follow letters directly)
     */
    private String normalize(String raw) {
        // Uppercase everything
        String upper = raw.toUpperCase();

        // Replace spaces with dash
        upper = upper.replaceAll("\\s+", "-");

        // If letters followed directly by digits with no dash, insert dash
        // e.g. PROJ5 → PROJ-5
        upper = upper.replaceAll("([A-Z])([0-9])", "$1-$2");

        // Remove any characters that aren't letters, digits, or dash
        upper = upper.replaceAll("[^A-Z0-9\\-]", "");

        // Collapse multiple dashes
        upper = upper.replaceAll("-{2,}", "-");

        return upper;
    }
}