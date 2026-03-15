# QA Orchestrator Platform - API Contract

## Overview

This document defines the API contract for the QA Orchestrator Platform.

The platform analyzes Jira issues and produces structured QA outputs including:

- Requirement analysis
- Test design
- Automation recommendation
- Risk evaluation
- Release recommendation

The current canonical response contract version is:

v2

---

## Base URL

### Production
https://qa-orchestrator-service.onrender.com

### Local
http://localhost:10000

---

## Primary Endpoint

### POST `/qa/api/v1/qa/analyze`

Analyzes a Jira issue and returns structured QA insights.

### Request Headers

Content-Type: application/json

### Request Body

{
  "issueKey": "PROJ-4"
}

### Request Fields

| Field    | Type   | Required | Description               |
|---------|--------|---------|---------------------------|
| issueKey | string | Yes | Jira issue key to analyze |

---

## Response Structure

### Top-Level Response

{
  "output": "QA Orchestrator Analysis...",
  "analysis": {
    "contractVersion": "v2",
    "traceabilityId": "PROJ-4",
    "analysisSummary": "Requirement status: READY. Automation: Hybrid (UI + API). Risk: HIGH (70). Release decision: Block.",
    "rawOutput": "QA Orchestrator Analysis...",
    "stages": {
      "requirement": {},
      "testDesign": {},
      "automation": {},
      "risk": {}
    }
  }
}

### Top-Level Fields

| Field | Type | Description |
|------|------|-------------|
| output | string | Human-readable QA analysis output |
| analysis | object | Structured QA analysis object |

---

## Analysis Object

### Canonical Fields

| Field | Type | Description |
|------|------|-------------|
| contractVersion | string | Response contract version |
| traceabilityId | string | Traceability identifier (Jira issue key) |
| analysisSummary | string | High-level summary of QA decision |
| rawOutput | string | Raw orchestrator output |
| stages | object | Canonical stage-based analysis output |

### Canonical Path

Primary structured response location:

analysis.stages

---

# Stage Artifacts

---

## Requirement Stage

Path:

analysis.stages.requirement

Example:

{
  "status": "READY",
  "featureSummary": "Coupon validation and application behavior during checkout.",
  "clarifiedRequirements": [
    "User can enter and apply a coupon code during checkout.",
    "Coupon input must be validated before discount is applied.",
    "Only one coupon can be applied per checkout session.",
    "Applied coupon state must persist within the same session."
  ],
  "edgeCases": [
    "Invalid coupon code is entered.",
    "User attempts to apply a second coupon after one is already active.",
    "Discount affects subtotal before tax calculation.",
    "Coupon remains applied after refresh within the same session."
  ],
  "openQuestions": [
    "Should expired coupons be rejected with a specific validation message?",
    "Is coupon stacking always disallowed, or only for this checkout flow?"
  ],
  "scope": [
    "Coupon entry and application flow",
    "Coupon validation behavior",
    "Single coupon enforcement"
  ],
  "outOfScope": [
    "Cross-device persistence",
    "Coupon management admin flows",
    "Promotion creation and configuration"
  ]
}

### Requirement Fields

| Field | Type | Description |
|------|------|-------------|
| status | string | Requirement readiness status |
| featureSummary | string | Feature summary |
| clarifiedRequirements | string[] | Extracted testable requirements |
| edgeCases | string[] | Edge case scenarios |
| openQuestions | string[] | Missing requirement questions |
| scope | string[] | Functional scope |
| outOfScope | string[] | Explicit exclusions |

---

## Test Design Stage

Path:

analysis.stages.testDesign

Example:

{
  "testScenarios": [
    "Valid coupon",
    "Invalid coupon",
    "Multiple coupon restriction",
    "Subtotal before tax",
    "Session persistence"
  ],
  "testCases": [
    {
      "id": "TC-01",
      "title": "Validate valid coupon application",
      "preconditions": "User has items in cart",
      "steps": [
        "Open checkout",
        "Enter valid coupon",
        "Apply coupon"
      ],
      "expectedResult": "Coupon applied successfully",
      "testType": "UI",
      "suiteTag": "Smoke",
      "testData": "Valid coupon",
      "priority": "High"
    }
  ]
}

### Test Case Fields

| Field | Type | Description |
|------|------|-------------|
| id | string | Test case ID |
| title | string | Test case title |
| preconditions | string | Preconditions |
| steps | string[] | Execution steps |
| expectedResult | string | Expected outcome |
| testType | string | UI / API / E2E |
| suiteTag | string | Smoke / Regression |
| testData | string | Test data |
| priority | string | Priority |

---

## Automation Stage

Path:

analysis.stages.automation

Example:

{
  "automationRecommendation": "Hybrid (UI + API)",
  "automationReasoning": "Checkout UI validation and API rule enforcement required",
  "coverageSplit": "UI 60% / API 40%",
  "frameworkSuggestion": "Java + Selenium + TestNG + REST Assured"
}

### Automation Fields

| Field | Type | Description |
|------|------|-------------|
| automationRecommendation | string | Automation strategy |
| automationReasoning | string | Explanation |
| coverageSplit | string | UI/API coverage |
| frameworkSuggestion | string | Suggested framework |

---

## Risk Stage

Path:

analysis.stages.risk

Example:

{
  "riskScore": 70,
  "riskLevel": "HIGH",
  "riskReason": "Checkout flow, financial impact",
  "topRiskDrivers": [
    "Checkout flow",
    "Financial impact"
  ],
  "releaseRecommendation": "Block"
}

### Risk Fields

| Field | Type | Description |
|------|------|-------------|
| riskScore | integer | Risk score |
| riskLevel | string | LOW / MEDIUM / HIGH |
| riskReason | string | Risk explanation |
| topRiskDrivers | string[] | Major drivers |
| releaseRecommendation | string | Release decision |

---

# Compatibility Fields

The response retains flat analysis fields for backward compatibility.

Examples:

- analysis.requirementStatus
- analysis.featureSummary
- analysis.automationRecommendation
- analysis.riskLevel
- analysis.riskScore
- analysis.releaseRecommendation
- analysis.clarifiedRequirements
- analysis.edgeCases
- analysis.testScenarios
- analysis.testCases

Legacy stage aliases are also present:

- analysis.requirementStage
- analysis.testDesignStage
- analysis.automationStage
- analysis.riskStage

These exist only for compatibility.

Canonical consumers should rely on:

analysis.stages

---

# Contract Versioning

Current version:

v2

Rules:

- Breaking changes require a new version
- New fields may be added without breaking compatibility
- Canonical consumers should use `analysis.stages`

---

# Local Testing

Start service:

export JIRA_BASE_URL=https://your-domain.atlassian.net
export JIRA_EMAIL=your-email@example.com
export JIRA_API_TOKEN=your-jira-api-token

./mvnw spring-boot:run

Test:

curl -X POST http://localhost:10000/qa/api/v1/qa/analyze \
-H "Content-Type: application/json" \
-d '{"issueKey":"PROJ-4"}'

---

# Notes

This contract supports:

- QA workflow orchestration
- AI-assisted test planning
- release decision support
- Copilot integrations
- dashboard visualization