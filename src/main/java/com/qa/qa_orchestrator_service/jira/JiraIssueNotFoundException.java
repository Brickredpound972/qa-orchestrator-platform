package com.qa.qa_orchestrator_service.jira;

/**
 * Thrown when a Jira issue is not found (404).
 * Caught by GlobalExceptionHandler to return a clean 404 response.
 */
public class JiraIssueNotFoundException extends RuntimeException {

    private final String issueKey;

    public JiraIssueNotFoundException(String issueKey) {
        super("Jira issue not found: " + issueKey + ". Please verify the issue key exists in your Jira project.");
        this.issueKey = issueKey;
    }

    public String getIssueKey() {
        return issueKey;
    }
}