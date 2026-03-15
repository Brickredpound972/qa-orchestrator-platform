package com.qa.qa_orchestrator_service.service.stage;

import com.qa.qa_orchestrator_service.model.QaAnalysisResult;
import com.qa.qa_orchestrator_service.model.QaStagesArtifact;
import org.springframework.stereotype.Component;

@Component
public class StageAggregationStage {

    public void apply(QaAnalysisResult result) {
        QaStagesArtifact stages = new QaStagesArtifact();
        stages.setRequirement(result.getRequirementStage());
        stages.setTestDesign(result.getTestDesignStage());
        stages.setAutomation(result.getAutomationStage());
        stages.setRisk(result.getRiskStage());

        result.setStages(stages);
    }
}