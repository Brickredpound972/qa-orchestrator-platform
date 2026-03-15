package com.qa.qa_orchestrator_service.service.stage;

import com.qa.qa_orchestrator_service.model.QaAnalysisResult;
import com.qa.qa_orchestrator_service.model.QaTestCase;
import com.qa.qa_orchestrator_service.model.TestDesignStageArtifact;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TestDesignStage {

    public void apply(QaAnalysisResult result, String raw) {
        List<String> scenarios = extractBulletSection(raw, "Test Strategy:");
        List<QaTestCase> generatedTestCases;

        result.setTestScenarios(scenarios);
        generatedTestCases = buildGeneratedTestCases(result);
        result.setTestCases(generatedTestCases);

        TestDesignStageArtifact artifact = new TestDesignStageArtifact();
        artifact.setTestScenarios(scenarios);
        artifact.setTestCases(generatedTestCases);

        result.setTestDesignStage(artifact);
    }

    private List<String> extractBulletSection(String raw, String sectionHeader) {
        List<String> items = new ArrayList<>();
        if (raw == null || sectionHeader == null) {
            return items;
        }

        String[] lines = raw.split("\\R");
        boolean inSection = false;

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.equals(sectionHeader.trim())) {
                inSection = true;
                continue;
            }

            if (inSection) {
                if (trimmed.isEmpty()) {
                    continue;
                }

                if (!trimmed.startsWith("-")) {
                    break;
                }

                items.add(trimmed.substring(1).trim());
            }
        }

        return items;
    }

    private List<QaTestCase> buildGeneratedTestCases(QaAnalysisResult result) {
        List<QaTestCase> testCases = new ArrayList<>();

        List<String> scenarios = result.getTestScenarios() != null
                ? result.getTestScenarios()
                : new ArrayList<>();

        List<String> edgeCases = result.getEdgeCases() != null
                ? result.getEdgeCases()
                : new ArrayList<>();

        int tcIndex = 1;

        for (String scenario : scenarios) {
            String normalized = scenario.toLowerCase();

            if (normalized.contains("invalid")) {
                testCases.add(new QaTestCase(
                        formatTestCaseId(tcIndex++),
                        "Reject invalid coupon code",
                        "User has items in cart and an active checkout session",
                        List.of("Open checkout", "Enter an invalid coupon", "Apply coupon"),
                        "System should reject the coupon and display validation feedback",
                        "UI",
                        "Regression",
                        "Invalid coupon",
                        "High"));
            } else if (normalized.contains("valid")) {
                testCases.add(new QaTestCase(
                        formatTestCaseId(tcIndex++),
                        "Validate valid coupon application",
                        "User has items in cart and an active checkout session",
                        List.of("Open checkout", "Enter a valid coupon", "Apply coupon"),
                        "System should accept the coupon and apply the discount",
                        "UI",
                        "Smoke",
                        "Valid coupon",
                        "High"));
            } else if (normalized.contains("multiple")) {
                testCases.add(new QaTestCase(
                        formatTestCaseId(tcIndex++),
                        "Prevent multiple coupon application",
                        "User already applied one valid coupon",
                        List.of("Apply first coupon", "Attempt to apply a second coupon"),
                        "System should enforce the single coupon restriction",
                        "UI",
                        "Regression",
                        "Two valid coupons",
                        "High"));
            } else if (normalized.contains("subtotal")) {
                testCases.add(new QaTestCase(
                        formatTestCaseId(tcIndex++),
                        "Validate subtotal calculation before tax",
                        "User has taxable items in cart and a valid coupon",
                        List.of("Open checkout", "Apply valid coupon", "Review subtotal before tax"),
                        "Discount should be reflected in subtotal before tax is calculated",
                        "API",
                        "Regression",
                        "Coupon + taxable cart",
                        "High"));
            } else if (normalized.contains("session")) {
                testCases.add(new QaTestCase(
                        formatTestCaseId(tcIndex++),
                        "Validate coupon persistence in same session",
                        "User has successfully applied a coupon",
                        List.of("Apply coupon", "Refresh the page within the same session"),
                        "Coupon state should persist during the same user session",
                        "E2E",
                        "Regression",
                        "Same session",
                        "Medium"));
            }
        }

        for (String edgeCase : edgeCases) {
            String normalized = edgeCase.toLowerCase();

            if (normalized.contains("invalid coupon")
                    && !containsTitle(testCases, "Reject invalid coupon code")) {
                testCases.add(new QaTestCase(
                        formatTestCaseId(tcIndex++),
                        "Reject invalid coupon code",
                        "User has items in cart and an active checkout session",
                        List.of("Open checkout", "Enter an invalid coupon", "Apply coupon"),
                        "System should reject the coupon and display validation feedback",
                        "UI",
                        "Regression",
                        "Invalid coupon",
                        "High"));
            }

            if (normalized.contains("second coupon")
                    && !containsTitle(testCases, "Prevent multiple coupon application")) {
                testCases.add(new QaTestCase(
                        formatTestCaseId(tcIndex++),
                        "Prevent multiple coupon application",
                        "User already applied one valid coupon",
                        List.of("Apply first coupon", "Attempt to apply a second coupon"),
                        "System should enforce the single coupon restriction",
                        "UI",
                        "Regression",
                        "Two valid coupons",
                        "High"));
            }

            if (normalized.contains("subtotal")
                    && !containsTitle(testCases, "Validate subtotal calculation before tax")) {
                testCases.add(new QaTestCase(
                        formatTestCaseId(tcIndex++),
                        "Validate subtotal calculation before tax",
                        "User has taxable items in cart and a valid coupon",
                        List.of("Open checkout", "Apply valid coupon", "Review subtotal before tax"),
                        "Discount should be reflected in subtotal before tax is calculated",
                        "API",
                        "Regression",
                        "Coupon + taxable cart",
                        "High"));
            }

            if ((normalized.contains("same session") || normalized.contains("refresh"))
                    && !containsTitle(testCases, "Validate coupon persistence in same session")) {
                testCases.add(new QaTestCase(
                        formatTestCaseId(tcIndex++),
                        "Validate coupon persistence in same session",
                        "User has successfully applied a coupon",
                        List.of("Apply coupon", "Refresh the page within the same session"),
                        "Coupon state should persist during the same user session",
                        "E2E",
                        "Regression",
                        "Same session",
                        "Medium"));
            }
        }

        return testCases;
    }

    private String formatTestCaseId(int index) {
        return String.format("TC-%02d", index);
    }

    private boolean containsTitle(List<QaTestCase> testCases, String title) {
        for (QaTestCase testCase : testCases) {
            if (testCase.getTitle() != null && testCase.getTitle().equalsIgnoreCase(title)) {
                return true;
            }
        }
        return false;
    }
}