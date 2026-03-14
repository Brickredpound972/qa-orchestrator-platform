package com.qa.qa_orchestrator_service.model;

import java.util.List;

public class QaTestCase {

    private String id;
    private String title;
    private String preconditions;
    private List<String> steps;
    private String expectedResult;
    private String testType;
    private String suiteTag;
    private String testData;
    private String priority;

    public QaTestCase() {
    }

    public QaTestCase(String id, String title, String preconditions, List<String> steps,
                      String expectedResult, String testType, String suiteTag,
                      String testData, String priority) {
        this.id = id;
        this.title = title;
        this.preconditions = preconditions;
        this.steps = steps;
        this.expectedResult = expectedResult;
        this.testType = testType;
        this.suiteTag = suiteTag;
        this.testData = testData;
        this.priority = priority;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPreconditions() {
        return preconditions;
    }

    public void setPreconditions(String preconditions) {
        this.preconditions = preconditions;
    }

    public List<String> getSteps() {
        return steps;
    }

    public void setSteps(List<String> steps) {
        this.steps = steps;
    }

    public String getExpectedResult() {
        return expectedResult;
    }

    public void setExpectedResult(String expectedResult) {
        this.expectedResult = expectedResult;
    }

    public String getTestType() {
        return testType;
    }

    public void setTestType(String testType) {
        this.testType = testType;
    }

    public String getSuiteTag() {
        return suiteTag;
    }

    public void setSuiteTag(String suiteTag) {
        this.suiteTag = suiteTag;
    }

    public String getTestData() {
        return testData;
    }

    public void setTestData(String testData) {
        this.testData = testData;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }
}