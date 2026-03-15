package com.qa.qa_orchestrator_service.service.stage;

import com.qa.qa_orchestrator_service.model.QaAnalysisResult;
import org.springframework.stereotype.Component;
import com.qa.qa_orchestrator_service.model.RequirementStageArtifact;

import java.util.ArrayList;
import java.util.List;

@Component
public class RequirementAnalysisStage {

    public void apply(QaAnalysisResult result, String raw) {
        String status = extractStatus(raw);
        String featureSummary = buildFeatureSummary(raw);
        List<String> clarifiedRequirements = buildClarifiedRequirements(raw);
        List<String> edgeCases = buildEdgeCases(raw);
        List<String> openQuestions = buildOpenQuestions(raw);
        List<String> scope = buildScope(raw);
        List<String> outOfScope = buildOutOfScope(raw);

        result.setRequirementStatus(status);
        result.setFeatureSummary(featureSummary);
        result.setClarifiedRequirements(clarifiedRequirements);
        result.setEdgeCases(edgeCases);
        result.setOpenQuestions(openQuestions);
        result.setScope(scope);
        result.setOutOfScope(outOfScope);

        RequirementStageArtifact artifact = new RequirementStageArtifact();
        artifact.setStatus(status);
        artifact.setFeatureSummary(featureSummary);
        artifact.setClarifiedRequirements(clarifiedRequirements);
        artifact.setEdgeCases(edgeCases);
        artifact.setOpenQuestions(openQuestions);
        artifact.setScope(scope);
        artifact.setOutOfScope(outOfScope);

        result.setRequirementStage(artifact);
    }

    private String extractStatus(String raw) {
        if (raw != null && raw.contains("Requirement Analysis: READY")) {
            return "READY";
        }
        if (raw != null && raw.contains("Requirement Analysis: BLOCKED")) {
            return "BLOCKED";
        }
        return "UNKNOWN";
    }

    private String buildFeatureSummary(String raw) {
        String lower = raw == null ? "" : raw.toLowerCase();

        if (lower.contains("coupon") && lower.contains("checkout")) {
            return "Coupon validation and application behavior during checkout.";
        }
        if (lower.contains("coupon")) {
            return "Coupon application and validation flow.";
        }
        if (lower.contains("checkout")) {
            return "Checkout-related validation and session behavior.";
        }

        return "QA analysis generated from available issue context.";
    }

    private List<String> buildClarifiedRequirements(String raw) {
        List<String> items = new ArrayList<>();
        String lower = raw == null ? "" : raw.toLowerCase();

        if (lower.contains("coupon")) {
            items.add("User can enter and apply a coupon code during checkout.");
        }
        if (lower.contains("validation")) {
            items.add("Coupon input must be validated before discount is applied.");
        }
        if (lower.contains("single coupon")
                || lower.contains("multiple coupon")
                || lower.contains("one coupon")) {
            items.add("Only one coupon can be applied per checkout session.");
        }
        if (lower.contains("session persistence")
                || lower.contains("same session")
                || lower.contains("session")) {
            items.add("Applied coupon state must persist within the same session.");
        }

        return items;
    }

    private List<String> buildEdgeCases(String raw) {
        List<String> items = new ArrayList<>();
        String lower = raw == null ? "" : raw.toLowerCase();

        if (lower.contains("invalid coupon")) {
            items.add("Invalid coupon code is entered.");
        }
        if (lower.contains("multiple coupon")) {
            items.add("User attempts to apply a second coupon after one is already active.");
        }
        if (lower.contains("subtotal")) {
            items.add("Discount affects subtotal before tax calculation.");
        }
        if (lower.contains("session")) {
            items.add("Coupon remains applied after refresh within the same session.");
        }

        return items;
    }

    private List<String> buildOpenQuestions(String raw) {
        List<String> items = new ArrayList<>();
        String lower = raw == null ? "" : raw.toLowerCase();

        if (!lower.contains("expiry") && lower.contains("coupon")) {
            items.add("Should expired coupons be rejected with a specific validation message?");
        }

        if (!lower.contains("stack") && lower.contains("coupon")) {
            items.add("Is coupon stacking always disallowed, or only for this checkout flow?");
        }

        if (!lower.contains("guest") && lower.contains("checkout")) {
            items.add("Does the coupon behavior differ for guest and authenticated users?");
        }

        if (!lower.contains("currency") && (lower.contains("subtotal") || lower.contains("discount"))) {
            items.add("Are there any currency, rounding, or localization rules affecting subtotal calculation?");
        }

        return items;
    }

    private List<String> buildScope(String raw) {
        List<String> items = new ArrayList<>();
        String lower = raw == null ? "" : raw.toLowerCase();

        if (lower.contains("coupon")) {
            items.add("Coupon entry and application flow");
        }
        if (lower.contains("validation")) {
            items.add("Coupon validation behavior");
        }
        if (lower.contains("multiple coupon")) {
            items.add("Single coupon enforcement");
        }
        if (lower.contains("subtotal")) {
            items.add("Discount impact on subtotal before tax");
        }
        if (lower.contains("session")) {
            items.add("Same-session coupon persistence");
        }

        return items;
    }

    private List<String> buildOutOfScope(String raw) {
        List<String> items = new ArrayList<>();
        items.add("Cross-device persistence");
        items.add("Coupon management admin flows");
        items.add("Promotion creation and configuration");
        return items;
    }
}