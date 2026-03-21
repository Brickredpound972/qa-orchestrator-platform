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
 * Orchestrates the full QA analysis pipeline.
 *
 * Stage execution order:
 * 1. RequirementAnalysisStage  — reads Jira JSON, extracts requirements
 * 2. TestDesignStage           — reads requirements, generates test cases
 * 3. AutomationDecisionStage   — reads test cases + risk, decides automation strategy
 * 4. RiskAnalysisStage         — reads all previous outputs, scores release risk
 * 5. BugReportStage            — reads all previous outputs, generates bug report template
 * 6. AnalysisSummaryStage      — generates human-readable summary line
 * 7. StageAggregationStage     — packages everything into analysis.stages
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

    public QaOrchestratorService(
            JiraClient jiraClient,
            RequirementAnalysisStage requirementAnalysisStage,
            TestDesignStage testDesignStage,
            AutomationDecisionStage automationDecisionStage,
            RiskAnalysisStage riskAnalysisStage,
            BugReportStage bugReportStage,
            AnalysisSummaryStage analysisSummaryStage,
            StageAggregationStage stageAggregationStage) {
        this.jiraClient = jiraClient;
        this.requirementAnalysisStage = requirementAnalysisStage;
        this.testDesignStage = testDesignStage;
        this.automationDecisionStage = automationDecisionStage;
        this.riskAnalysisStage = riskAnalysisStage;
        this.bugReportStage = bugReportStage;
        this.analysisSummaryStage = analysisSummaryStage;
        this.stageAggregationStage = stageAggregationStage;
    }

    public QaAnalysisResult runAnalysis(String issueKey) {
        String jiraJson = jiraClient.getIssue(issueKey);
        QaAnalysisResult result = runPipeline(issueKey, jiraJson);
        jiraClient.addComment(issueKey, result.getAnalysisSummary());
        return result;
    }

    private QaAnalysisResult runPipeline(String issueKey, String jiraJson) {
        QaAnalysisResult result = new QaAnalysisResult();
        result.setTraceabilityId(issueKey);
        result.setContractVersion("v2");
        result.setRawOutput(jiraJson);

        requirementAnalysisStage.apply(result, jiraJson);
        testDesignStage.apply(result, jiraJson);
        automationDecisionStage.apply(result, jiraJson);
        riskAnalysisStage.apply(result, jiraJson);
        bugReportStage.apply(result, jiraJson);
        analysisSummaryStage.apply(result);
        stageAggregationStage.apply(result);

        return result;
    }

    public QaAnalysisResult buildStructuredAnalysis(String issueKey, String raw) {
        return runPipeline(issueKey, raw);
    }
}