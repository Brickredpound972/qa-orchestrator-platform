package com.qa.qa_orchestrator_service.model;

public class QaAnalyzeResponse {

    private String output;

    public QaAnalyzeResponse() {
    }

    public QaAnalyzeResponse(String output) {
        this.output = output;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }
}