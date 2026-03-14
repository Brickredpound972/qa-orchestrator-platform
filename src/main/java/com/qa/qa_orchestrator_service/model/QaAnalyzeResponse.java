package com.qa.qa_orchestrator_service.model;

public class QaAnalyzeResponse {

    private String output;
    private QaAnalysisResult analysis;

    public QaAnalyzeResponse() {
    }

    public QaAnalyzeResponse(String output) {
        this.output = output;
    }

    public QaAnalyzeResponse(String output, QaAnalysisResult analysis) {
        this.output = output;
        this.analysis = analysis;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public QaAnalysisResult getAnalysis() {
        return analysis;
    }

    public void setAnalysis(QaAnalysisResult analysis) {
        this.analysis = analysis;
    }
}