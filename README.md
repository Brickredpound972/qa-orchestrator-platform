# QA Orchestrator Platform

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/SpringBoot-3-green)
![API](https://img.shields.io/badge/API-Live-brightgreen)
![Contract](https://img.shields.io/badge/Contract-v2-orange)

**QA Orchestrator Platform** is an AI-powered QA decision and orchestration engine that analyzes Jira issues and generates structured QA insights including test strategies, automation recommendations, and release risk analysis.

The platform acts as a **QA decision engine**, helping engineering teams determine:

- how a feature should be tested
- what should be automated
- what risks the change introduces
- whether the feature is safe to release

Instead of manually analyzing tickets, the system converts Jira issues into structured QA intelligence.

---

# Key Features

- Jira issue ingestion via REST API
- Multi-stage QA analysis pipeline
- Structured test scenario and test case generation
- Automation strategy recommendation (UI / API / Hybrid)
- Risk scoring and release recommendation
- Versioned API response contract
- Copilot Studio and Power Automate integration support

---

# System Architecture

The platform integrates with enterprise workflow tools and processes Jira issues through a structured QA analysis pipeline.
```
User / Copilot Studio / Power Automate
            │
            v
      Custom Connector
            │
            v
    QA Orchestrator API
            │
            v
     Spring Boot Service
            │
            v
        Jira REST API
```
The system retrieves Jira issue data and executes a multi-stage QA pipeline to generate testing artifacts and release risk analysis.

---

# QA Analysis Pipeline

Each Jira issue is processed through structured QA stages.

## 1. Requirement Analysis

- Extracts testable requirements
- Identifies missing information
- Detects scope boundaries
- Produces requirement-stage artifacts

---

## 2. Test Design

- Generates test scenarios
- Produces structured test cases
- Identifies edge cases
- Produces test-design-stage artifacts

---

## 3. Automation Decision

- Determines automation strategy
- Suggests UI / API / Hybrid coverage
- Recommends testing frameworks
- Produces automation-stage artifacts

---

## 4. Risk Analysis

- Calculates feature risk score
- Identifies major risk drivers
- Provides release recommendation
- Produces risk-stage artifacts

---

# Live API

Production endpoint

https://qa-orchestrator-service.onrender.com

---

# Example Request

```bash
curl -X POST https://qa-orchestrator-service.onrender.com/qa/api/v1/qa/analyze \
-H "Content-Type: application/json" \
-d '{"issueKey":"PROJ-4"}'
```
---

# Example Response

```json
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
```
---

## API Contract

The API returns a versioned response contract.

Current contract version:
v2

Primary structured output is available under:
analysis.stages

Top-level fields remain as a compatibility layer for earlier consumers.

---

## Purpose

Modern QA teams spend significant time answering questions such as:
- How should this feature be tested?
- What should be automated?
- What risks does this change introduce?
- Is this feature safe to release?

QA Orchestrator automates this reasoning process by analyzing Jira issues and producing structured QA guidance.

---

## Tech Stack

### Backend
- Java 17
- Spring Boot
- Maven

### Infrastructure
- Docker
- Render (Cloud Deployment)

### Integrations
- Jira REST API
- Microsoft Copilot Studio
- Power Automate

---

## Environment Setup

The application uses environment variables for Jira integration.

Required variables:
JIRA_BASE_URL
JIRA_EMAIL
JIRA_API_TOKEN

Example:
```bash
JIRA_BASE_URL=https://your-domain.atlassian.net
JIRA_EMAIL=your-email@example.com
JIRA_API_TOKEN=your-jira-api-token
```

---

## Local Development

Export environment variables before starting the application.

macOS / zsh
```bash
export JIRA_BASE_URL=https://your-domain.atlassian.net
export JIRA_EMAIL=your-email@example.com
export JIRA_API_TOKEN=your-jira-api-token
```

Run the application:
```bash
./mvnw spring-boot:run
```

Test locally:
```bash
curl -X POST http://localhost:10000/qa/api/v1/qa/analyze \
-H "Content-Type: application/json" \
-d '{"issueKey":"PROJ-4"}'
```

---

## Security Notes
- Never commit credentials to the repository
- Do not store secrets in source control
- Use environment variables or a secure secret manager
- Keep production credentials outside the codebase

---

## Project Goal

QA Orchestrator Platform is designed to support modern QA teams by:
- analyzing Jira requirements
- generating structured test strategies
- identifying testing risks
- recommending automation approaches
- supporting release decisions
- integrating with AI-assisted workflow tools

---

## Future Improvements

Planned enhancements include:
- Response contract cleanup to reduce compatibility fields
- Bug report generation stage
- Test execution planning stage
- Smarter requirement ambiguity detection
- Expanded release decision logic
- Web dashboard for QA insights

---

## Documentation

Project documentation is available in the `/docs` directory.
- API Contract → docs/api_contract.md
- Architecture → docs/architecture.md
- Roadmap → docs/roadmap.md

---

## API Endpoints

Current QA analysis endpoints:
```
POST /qa/api/v1/qa/analyze
POST /qa/analyze
POST /qa/run/{issueKey}
```