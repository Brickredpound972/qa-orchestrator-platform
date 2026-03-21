package com.qa.qa_orchestrator_service.service;

import com.qa.qa_orchestrator_service.jira.JiraClient;
import com.qa.qa_orchestrator_service.model.QaAnalysisResult;
import com.qa.qa_orchestrator_service.service.stage.RequirementAnalysisStage;
import com.qa.qa_orchestrator_service.service.stage.TestDesignStage;
import com.qa.qa_orchestrator_service.service.stage.AutomationDecisionStage;
import com.qa.qa_orchestrator_service.service.stage.RiskAnalysisStage;
import com.qa.qa_orchestrator_service.service.stage.BugReportStage;
import com.qa.qa_orchestrator_service.service.stage.AnalysisSummaryStage;
import com.qa.qa_orchestrator_service.service.stage.StageAggregationStage;
import org.springframework.stereotype.Service;

/**
 * QaOrchestratorService
 *
 * Orchestrates the full QA analysis pipeline with structured logging.
 * Each stage is timed and logged individually.
 */
@Service
public class QaOrchestratorService {

    private final JiraClient jiraClient;
    private final RequirementAnalysisStage requirementAnalysisStage;
    private final TestDesignStage testDesignStage;
    private final AutomationDecisionStage automationDecisionStage;
    private final RiskAnalysisStage riskAnalysisStage;
    private final BugReportStage bugReportStage;
    private final AnalysisSummaryStage analysisSummaryStage;
    private final StageAggregationStage stageAggregationStage;
    private final PipelineLogger pipelineLogger;

    public QaOrchestratorService(
            JiraClient jiraClient,
            RequirementAnalysisStage requirementAnalysisStage,
            TestDesignStage testDesignStage,
            AutomationDecisionStage automationDecisionStage,
            RiskAnalysisStage riskAnalysisStage,
            BugReportStage bugReportStage,
            AnalysisSummaryStage analysisSummaryStage,
            StageAggregationStage stageAggregationStage,
            PipelineLogger pipelineLogger) {
        this.jiraClient = jiraClient;
        this.requirementAnalysisStage = requirementAnalysisStage;
        this.testDesignStage = testDesignStage;
        this.automationDecisionStage = automationDecisionStage;
        this.riskAnalysisStage = riskAnalysisStage;
        this.bugReportStage = bugReportStage;
        this.analysisSummaryStage = analysisSummaryStage;
        this.stageAggregationStage = stageAggregationStage;
        this.pipelineLogger = pipelineLogger;
    }

    public QaAnalysisResult runAnalysis(String issueKey) {
        // Fetch Jira issue
        long jiraStart = System.currentTimeMillis();
        String jiraJson;
        try {
            jiraJson = jiraClient.getIssue(issueKey);
            pipelineLogger.jiraFetch(issueKey, System.currentTimeMillis() - jiraStart);
        } catch (Exception e) {
            pipelineLogger.jiraError(issueKey, e.getMessage());
            throw e;
        }

        // Run pipeline
        QaAnalysisResult result = runPipeline(issueKey, jiraJson);

        // Post Jira comment
        try {
            jiraClient.addComment(issueKey, result.getAnalysisSummary());
        } catch (Exception e) {
            pipelineLogger.jiraError(issueKey, "Comment failed: " + e.getMessage());
        }

        return result;
    }

    private QaAnalysisResult runPipeline(String issueKey, String jiraJson) {
        long pipelineStart = System.currentTimeMillis();
        pipelineLogger.pipelineStart(issueKey);

        QaAnalysisResult result = new QaAnalysisResult();
        result.setTraceabilityId(issueKey);
        result.setContractVersion("v2");
        result.setRawOutput(jiraJson);

        // Stage 1 — Requirement Analysis
        runStage(issueKey, "requirement", () ->
                requirementAnalysisStage.apply(result, jiraJson));

        // Quality gate — stop if BLOCKED
        if ("BLOCKED".equalsIgnoreCase(result.getRequirementStatus())) {
            pipelineLogger.blockedPipeline(issueKey);
            analysisSummaryStage.apply(result);
            stageAggregationStage.apply(result);
            pipelineLogger.pipelineEnd(issueKey,
                    System.currentTimeMillis() - pipelineStart,
                    result.getRequirementStatus(), result.getRiskLevel(),
                    result.getRiskScore(), result.getReleaseRecommendation());
            return result;
        }

        // Stage 2 — Test Design
        runStage(issueKey, "test_design", () ->
                testDesignStage.apply(result, jiraJson));

        // Stage 3 — Automation Decision
        runStage(issueKey, "automation", () ->
                automationDecisionStage.apply(result, jiraJson));

        // Stage 4 — Risk Analysis
        runStage(issueKey, "risk", () ->
                riskAnalysisStage.apply(result, jiraJson));

        // Stage 5 — Bug Report
        runStage(issueKey, "bug_report", () ->
                bugReportStage.apply(result, jiraJson));

        // Stage 6 — Summary + Aggregation
        analysisSummaryStage.apply(result);
        stageAggregationStage.apply(result);

        long totalDuration = System.currentTimeMillis() - pipelineStart;
        pipelineLogger.pipelineEnd(issueKey, totalDuration,
                result.getRequirementStatus(), result.getRiskLevel(),
                result.getRiskScore(), result.getReleaseRecommendation());

        return result;
    }

    /**
     * Runs a stage with timing and error logging.
     * Stage errors are caught here — pipeline continues with fallback values.
     */
    private void runStage(String issueKey, String stageName, Runnable stage) {
        long start = System.currentTimeMillis();
        pipelineLogger.stageStart(issueKey, stageName);
        try {
            stage.run();
            pipelineLogger.stageEnd(issueKey, stageName, System.currentTimeMillis() - start);
        } catch (Exception e) {
            pipelineLogger.stageError(issueKey, stageName, e.getMessage());
        }
    }

    public QaAnalysisResult buildStructuredAnalysis(String issueKey, String raw) {
        return runPipeline(issueKey, raw);
    }
}