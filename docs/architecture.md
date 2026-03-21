# QA Orchestrator Platform — Architecture

## System Overview

QA Orchestrator Platform is an AI-powered QA decision engine that analyzes Jira issues and produces structured QA intelligence.

Each pipeline stage is powered by a large language model (LLM). Stages are not independent — each stage reads from the previous stage output and builds on it. This is what makes it a real pipeline, not a collection of isolated analyzers.

---

## High Level Architecture

```
User / Copilot Studio / Power Automate
            │
            ▼
      Custom API Connector
            │
            ▼
      QA Orchestrator API
            │
            ▼
      Spring Boot Service
         │         │
         ▼         ▼
   Jira REST    Groq LLM
      API          API
```

---

## Pipeline Flow

```
Jira Issue JSON
      │
      ▼
RequirementAnalysisStage
  → reads: raw Jira JSON
  → produces: clarifiedRequirements, edgeCases, openQuestions, scope
      │
      ▼
TestDesignStage
  → reads: clarifiedRequirements, edgeCases
  → produces: testScenarios, testCases (with UI/API/E2E types)
      │
      ▼
AutomationDecisionStage
  → reads: testCases (type distribution), riskLevel, scope
  → produces: automationRecommendation, coverageSplit, frameworkSuggestion
      │
      ▼
RiskAnalysisStage
  → reads: requirements, testCases, openQuestions, scope
  → produces: riskScore, riskLevel, topRiskDrivers, releaseRecommendation
      │
      ▼
BugReportStage
  → reads: full pipeline context
  → produces: bug report template (title, severity, reproductionSteps, impactSummary)
      │
      ▼
AnalysisSummaryStage
  → reads: key fields from all stages
  → produces: analysisSummary (human-readable one-liner)
      │
      ▼
StageAggregationStage
  → packages all stage artifacts into analysis.stages
      │
      ▼
Structured QA Response (analysis.stages)
```

---

## Technology Stack

### Backend
- Java 17
- Spring Boot 3
- Maven

### LLM
- Groq API (Llama 3.3 70B)
- Pluggable — can switch to Claude or OpenAI by replacing GroqClient

### Infrastructure
- Docker
- Render Cloud

### Integrations
- Jira REST API
- Microsoft Copilot Studio
- Power Automate
- Custom API Connector

---

## Package Structure

```
com.qa.qa_orchestrator_service
├── controller
│   └── QaController.java
├── jira
│   └── JiraClient.java
├── model
│   ├── QaAnalysisResult.java
│   ├── QaAnalyzeRequest.java
│   ├── QaAnalyzeResponse.java
│   ├── QaStagesArtifact.java
│   ├── QaTestCase.java
│   ├── RequirementStageArtifact.java
│   ├── TestDesignStageArtifact.java
│   ├── AutomationStageArtifact.java
│   ├── RiskStageArtifact.java
│   └── BugReportStageArtifact.java
└── service
    ├── QaOrchestratorService.java
    ├── llm
    │   └── GroqClient.java
    └── stage
        ├── RequirementAnalysisStage.java
        ├── TestDesignStage.java
        ├── AutomationDecisionStage.java
        ├── RiskAnalysisStage.java
        ├── BugReportStage.java
        ├── AnalysisSummaryStage.java
        └── StageAggregationStage.java
```

---

## Key Design Decisions

**Stages feed each other** — TestDesignStage does not re-read the Jira JSON. It reads `clarifiedRequirements` and `edgeCases` from RequirementStage output. This is the core pipeline pattern.

**LLM client is pluggable** — `GroqClient` is a `@Component` injected into each stage. Switching to Claude or OpenAI requires replacing only this class.

**Graceful fallback on LLM error** — every stage has a fallback that prevents pipeline crash. If Groq fails on one stage, the pipeline continues with a default artifact.

**Single pipeline execution per request** — the controller calls `runAnalysis()` once. Pipeline does not run twice.

---

## Future Architecture

- QA Context Service — historical ticket and bug awareness per component
- Coverage-aware risk scoring — adjusts risk based on existing test coverage
- Release decision engine — structured go/no-go logic with override support
- Web dashboard — visualizes QA insights per sprint or release