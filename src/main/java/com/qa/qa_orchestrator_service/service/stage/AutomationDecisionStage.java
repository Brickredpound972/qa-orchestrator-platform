package com.qa.qa_orchestrator_service.service.stage;

import com.qa.qa_orchestrator_service.model.QaAnalysisResult;
import org.springframework.stereotype.Component;

@Component
public class AutomationDecisionStage {

    public void apply(QaAnalysisResult result, String raw) {
        String recommendation = result.getAutomationRecommendation();

        if (recommendation == null || recommendation.isBlank()) {
            recommendation = "Hybrid (UI + API)";
            result.setAutomationRecommendation(recommendation);
        }

        if ("Hybrid (UI + API)".equalsIgnoreCase(recommendation)) {
            result.setAutomationReasoning(
                    "The feature requires UI validation for checkout flow behavior and API-level validation for subtotal, discount, and rule enforcement."
            );
            result.setCoverageSplit("UI 60% / API 40%");
            result.setFrameworkSuggestion("Java + Selenium + TestNG + REST Assured");
            return;
        }

        if ("Automation (API-heavy)".equalsIgnoreCase(recommendation)) {
            result.setAutomationReasoning(
                    "The feature is primarily driven by backend validation and calculation logic, so API automation should dominate coverage."
            );
            result.setCoverageSplit("UI 20% / API 80%");
            result.setFrameworkSuggestion("Java + REST Assured + TestNG");
            return;
        }

        if ("Automation (UI-heavy)".equalsIgnoreCase(recommendation)) {
            result.setAutomationReasoning(
                    "The feature is primarily exercised through user-facing checkout interactions, making UI automation the dominant coverage layer."
            );
            result.setCoverageSplit("UI 80% / API 20%");
            result.setFrameworkSuggestion("Java + Selenium + TestNG");
            return;
        }

        if ("Manual".equalsIgnoreCase(recommendation)) {
            result.setAutomationReasoning(
                    "The feature currently lacks sufficient stable signals for reliable automation and should be validated manually first."
            );
            result.setCoverageSplit("UI 0% / API 0%");
            result.setFrameworkSuggestion("Manual validation only");
            return;
        }

        result.setAutomationReasoning(
                "Automation approach derived from available issue context and current QA policy."
        );
        result.setCoverageSplit("UI 50% / API 50%");
        result.setFrameworkSuggestion("Java + Selenium + TestNG + REST Assured");
    }
}