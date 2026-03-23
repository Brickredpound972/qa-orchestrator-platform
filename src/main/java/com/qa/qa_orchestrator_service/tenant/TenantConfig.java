package com.qa.qa_orchestrator_service.tenant;

/**
 * TenantConfig
 *
 * Phase 9 — Multi-tenant foundation.
 * Each tenant has isolated Jira credentials and LLM provider config.
 * Currently single-tenant — this is the foundation for future expansion.
 *
 * To add a new tenant:
 * 1. Add tenant entry to application.yml under tenants:
 * 2. Pass X-Tenant-ID header in requests
 * 3. TenantContextHolder resolves the correct config
 */
public class TenantConfig {

    private String tenantId;
    private String jiraBaseUrl;
    private String jiraEmail;
    private String jiraApiToken;
    private String llmProvider;
    private String llmApiKey;
    private boolean commentEnabled;

    public TenantConfig() {}

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getJiraBaseUrl() { return jiraBaseUrl; }
    public void setJiraBaseUrl(String jiraBaseUrl) { this.jiraBaseUrl = jiraBaseUrl; }
    public String getJiraEmail() { return jiraEmail; }
    public void setJiraEmail(String jiraEmail) { this.jiraEmail = jiraEmail; }
    public String getJiraApiToken() { return jiraApiToken; }
    public void setJiraApiToken(String jiraApiToken) { this.jiraApiToken = jiraApiToken; }
    public String getLlmProvider() { return llmProvider; }
    public void setLlmProvider(String llmProvider) { this.llmProvider = llmProvider; }
    public String getLlmApiKey() { return llmApiKey; }
    public void setLlmApiKey(String llmApiKey) { this.llmApiKey = llmApiKey; }
    public boolean isCommentEnabled() { return commentEnabled; }
    public void setCommentEnabled(boolean commentEnabled) { this.commentEnabled = commentEnabled; }
}