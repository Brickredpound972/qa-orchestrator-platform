# QA Orchestrator Platform

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/SpringBoot-3-green)
![API](https://img.shields.io/badge/API-Live-brightgreen)
![Contract](https://img.shields.io/badge/Contract-v2-orange)

AI-powered QA orchestration platform that analyzes Jira issues and produces structured QA insights including test strategy, risk assessment, and automation recommendations.

The system acts as a **QA decision engine**, helping teams determine how a feature should be tested, what should be automated, and whether it is safe to release.

---

## Key Features

- Jira issue ingestion via REST API
- Multi-stage QA analysis pipeline
- Structured test scenario and test case generation
- Automation strategy recommendation (UI / API / Hybrid)
- Risk scoring and release recommendation
- Versioned API response contract
- Copilot Studio and Power Automate integration support

---

## System Architecture

```text
Copilot Studio / Power Automate
        ↓
Custom Connector
        ↓
QA Orchestrator API
        ↓
Spring Boot Service
        ↓
Jira REST API

The platform retrieves Jira issue details and runs a multi-stage QA analysis pipeline to generate testing artifacts and risk evaluation.

⸻

QA Analysis Pipeline

The platform processes each Jira issue through structured stages:

1. Requirement Analysis
	•	Extracts testable requirements
	•	Identifies missing information
	•	Detects scope boundaries
	•	Produces requirement-stage artifact output

2. Test Design
	•	Generates test scenarios
	•	Produces structured test cases
	•	Identifies edge cases
	•	Produces test-design-stage artifact output

3. Automation Decision
	•	Determines automation strategy
	•	Suggests UI / API / Hybrid coverage
	•	Recommends testing frameworks
	•	Produces automation-stage artifact output

4. Risk Analysis
	•	Calculates feature risk score
	•	Identifies major risk drivers
	•	Provides release recommendation
	•	Produces risk-stage artifact output

⸻

Live API

Production endpoint:
https://qa-orchestrator-service.onrender.com

⸻

Example Request
curl -X POST https://qa-orchestrator-service.onrender.com/qa/api/v1/qa/analyze \
-H "Content-Type: application/json" \
-d '{"issueKey":"PROJ-4"}'

⸻

Example Response
{
  "output": "QA Orchestrator Analysis...",
  "analysis": {
    "traceabilityId": "PROJ-4",
    "contractVersion": "v2",
    "analysisSummary": "Requirement status: READY. Automation: Hybrid (UI + API). Risk: HIGH (70). Release decision: Block.",
    "automationRecommendation": "Hybrid (UI + API)",
    "riskLevel": "HIGH",
    "riskScore": 70,
    "releaseRecommendation": "Block",
    "stages": {
      "requirement": {
        "status": "READY",
        "featureSummary": "Coupon validation and application behavior during checkout."
      },
      "testDesign": {
        "testScenarios": [
          "Valid coupon",
          "Invalid coupon",
          "Multiple coupon restriction"
        ]
      },
      "automation": {
        "automationRecommendation": "Hybrid (UI + API)",
        "coverageSplit": "UI 60% / API 40%"
      },
      "risk": {
        "riskScore": 70,
        "riskLevel": "HIGH",
        "releaseRecommendation": "Block"
      }
    }
  }
}

⸻

API Contract

The API returns a versioned response contract.

Current contract version:
v2

Primary structured output is available under:
analysis.stages

Top-level flat fields are currently retained as a compatibility layer for earlier consumers.

⸻

Purpose

Modern QA teams spend significant time answering questions like:
	•	How should this feature be tested?
	•	What should be automated?
	•	What risks does this change introduce?
	•	Is this feature safe to release?

QA Orchestrator automates this decision process by analyzing Jira issues and producing structured QA guidance.

⸻

Tech Stack
	•	Java 17
	•	Spring Boot
	•	Maven
	•	Docker
	•	Render (Cloud Deployment)
	•	Jira REST API
	•	Microsoft Copilot Studio
	•	Power Automate

⸻

Environment Setup

The application uses environment variables for Jira integration.

Required variables:
JIRA_BASE_URL
JIRA_EMAIL
JIRA_API_TOKEN

Example:
JIRA_BASE_URL=https://your-domain.atlassian.net
JIRA_EMAIL=your-email@example.com
JIRA_API_TOKEN=your-jira-api-token


⸻

Local Development

Export environment variables before starting the application.

macOS / zsh
export JIRA_BASE_URL=https://your-domain.atlassian.net
export JIRA_EMAIL=your-email@example.com
export JIRA_API_TOKEN=your-jira-api-token

Run the application:
./mvnw spring-boot:run

Test locally:
curl -X POST http://localhost:10000/qa/api/v1/qa/analyze \
-H "Content-Type: application/json" \
-d '{"issueKey":"PROJ-4"}'

---

## Security Notes

- Never commit credentials to the repository
- Do not store secrets in source control
- Use environment variables or a secure secret manager
- Keep production credentials outside the codebase

---

## Project Goal

QA Orchestrator Platform is designed to support modern QA teams by:

- Analyzing Jira requirements
- Generating structured test strategy
- Identifying testing risks
- Recommending automation approach
- Supporting release decisions
- Integrating with AI-assisted workflow tools

---

## Future Improvements

- Response contract cleanup to reduce duplicate compatibility fields
- Additional stage artifacts for bug reporting and execution planning
- Smarter requirement ambiguity detection
- Expanded release decision rules
- Web dashboard for stage-level QA insights