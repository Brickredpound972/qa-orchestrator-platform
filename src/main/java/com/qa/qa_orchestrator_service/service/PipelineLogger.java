package com.qa.qa_orchestrator_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * PipelineLogger
 *
 * Structured logging for the QA analysis pipeline.
 *
 * Logs:
 * - Pipeline start/end with total duration
 * - Each stage start/end with duration
 * - Stage errors and fallbacks
 * - Jira API calls
 * - LLM call results
 *
 * All logs go to stdout — visible in Render dashboard logs.
 *
 * Format: [QA-PIPELINE] PROJ-4 | stage=requirement | duration=2341ms | status=OK
 */
@Component
public class PipelineLogger {

    private static final Logger log = LoggerFactory.getLogger(PipelineLogger.class);
    private static final String PREFIX = "[QA-PIPELINE]";

    public void pipelineStart(String issueKey) {
        log.info("{} {} | event=pipeline_start", PREFIX, issueKey);
    }

    public void pipelineEnd(String issueKey, long durationMs, String requirementStatus,
                            String riskLevel, Integer riskScore, String releaseRecommendation) {
        log.info("{} {} | event=pipeline_end | duration={}ms | requirement={} | risk={} | score={} | release={}",
                PREFIX, issueKey, durationMs,
                requirementStatus, riskLevel, riskScore, releaseRecommendation);
    }

    public void stageStart(String issueKey, String stage) {
        log.info("{} {} | event=stage_start | stage={}", PREFIX, issueKey, stage);
    }

    public void stageEnd(String issueKey, String stage, long durationMs) {
        log.info("{} {} | event=stage_end | stage={} | duration={}ms | status=OK",
                PREFIX, issueKey, stage, durationMs);
    }

    public void stageError(String issueKey, String stage, String errorMessage) {
        log.error("{} {} | event=stage_error | stage={} | error={}",
                PREFIX, issueKey, stage, errorMessage);
    }

    public void stageFallback(String issueKey, String stage) {
        log.warn("{} {} | event=stage_fallback | stage={} | using_fallback=true",
                PREFIX, issueKey, stage);
    }

    public void jiraFetch(String issueKey, long durationMs) {
        log.info("{} {} | event=jira_fetch | duration={}ms | status=OK",
                PREFIX, issueKey, durationMs);
    }

    public void jiraError(String issueKey, String errorMessage) {
        log.error("{} {} | event=jira_error | error={}", PREFIX, issueKey, errorMessage);
    }

    public void llmCall(String issueKey, String stage, long durationMs) {
        log.info("{} {} | event=llm_call | stage={} | duration={}ms | status=OK",
                PREFIX, issueKey, stage, durationMs);
    }

    public void llmError(String issueKey, String stage, String errorMessage) {
        log.error("{} {} | event=llm_error | stage={} | error={}",
                PREFIX, issueKey, stage, errorMessage);
    }

    public void blockedPipeline(String issueKey) {
        log.warn("{} {} | event=pipeline_blocked | reason=requirement_status_BLOCKED",
                PREFIX, issueKey);
    }
}