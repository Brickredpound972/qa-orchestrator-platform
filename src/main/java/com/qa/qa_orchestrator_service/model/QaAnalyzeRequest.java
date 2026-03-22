package com.qa.qa_orchestrator_service.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * QaAnalyzeRequest
 *
 * Request body for the QA analysis endpoint.
 *
 * Validates issueKey format before the pipeline runs.
 * Valid examples: PROJ-1, QA-123, MYPROJECT-4567
 */
public class QaAnalyzeRequest {

    @NotBlank(message = "issueKey is required and cannot be blank")
    @Size(max = 50, message = "issueKey must not exceed 50 characters")
    @Pattern(
        regexp = "^[A-Z][A-Z0-9_]+-[0-9]+$",
        message = "issueKey must follow Jira format: PROJECT-NUMBER (e.g. PROJ-4)"
    )
    private String issueKey;

    public String getIssueKey() {
        return issueKey;
    }

    public void setIssueKey(String issueKey) {
        this.issueKey = issueKey != null ? issueKey.trim().toUpperCase() : null;
    }
}