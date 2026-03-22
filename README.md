# QA Orchestrator Platform

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/SpringBoot-3-green)
![API](https://img.shields.io/badge/API-Live-brightgreen)
![Contract](https://img.shields.io/badge/Contract-v2-orange)
![LLM](https://img.shields.io/badge/LLM-Groq%20%2F%20Llama%203.3-purple)

**QA Orchestrator Platform** is an AI-powered QA decision engine that analyzes Jira issues and produces structured QA intelligence — including requirement analysis, test cases, automation strategy, risk scoring, and bug report templates.

Each stage is powered by a large language model (LLM). The system reasons about the feature like a senior QA engineer, not a keyword matcher.

---

## What It Does

Instead of manually analyzing a ticket, the system does this automatically:

```
Developer writes Jira ticket
        ↓
QA Orchestrator reads the ticket
        ↓
Requirement Analysis   → clarified requirements, edge cases, open questions
        ↓
Test Design            → test scenarios, structured test cases
        ↓
Automation Decision    → strategy, coverage split, framework recommendation
        ↓
Risk Analysis          → risk score, risk drivers, release recommendation
        ↓
Bug Report Template    → pre-filled bug report ready for QA engineer
```

---

## System Architecture

```
User / Copilot Studio / Power Automate
            │
            ▼
      Custom Connector
            │
            ▼
    QA Orchestrator API
            │
            ▼
     Spring Boot Service
       │           │
       ▼           ▼
  Jira REST API   Groq LLM API
```

---

## QA Analysis Pipeline

All 5 stages are LLM-powered. Each stage reads from the previous stage output — they are not independent.

### Stage 1 — Requirement Analysis
Reads the raw Jira JSON. Produces:
- `status` (READY / BLOCKED)
- `featureSummary`
- `clarifiedRequirements`
- `edgeCases`
- `openQuestions`
- `scope` / `outOfScope`

### Stage 2 — Test Design
Reads clarified requirements and edge cases from Stage 1. Produces:
- `testScenarios`
- `testCases` (id, title, preconditions, steps, expectedResult, testType, suiteTag, testData, priority)

### Stage 3 — Automation Decision
Reads test case types (UI/API/E2E counts) and risk level. Produces:
- `automationRecommendation` (Manual / UI-heavy / API-heavy / Hybrid)
- `automationReasoning`
- `coverageSplit`
- `frameworkSuggestion`

### Stage 4 — Risk Analysis
Reads all previous stage outputs. Produces:
- `riskScore` (0–100)
- `riskLevel` (LOW / MEDIUM / HIGH)
- `riskReason`
- `topRiskDrivers`
- `releaseRecommendation` (Go / Caution / Block)

### Stage 5 — Bug Report
Reads full pipeline context. Produces:
- `title`
- `severity` / `priority`
- `reproductionSteps`
- `expectedResult` / `actualResult`
- `impactSummary`
- `affectedAreas`
- `suggestedAssignee`

---

## Live API

Production endpoint:

```
https://qa-orchestrator-service.onrender.com
```

Health check:

```
https://qa-orchestrator-service.onrender.com/qa/health
```

---

## Example Request

```bash
curl -X POST https://qa-orchestrator-service.onrender.com/qa/api/v1/qa/analyze \
-H "Content-Type: application/json" \
-d '{"issueKey":"PROJ-4"}'
```

---

## Example Response (condensed)

```json
{
  "output": "Requirement status: READY. Automation: Hybrid (UI + API). Risk: MEDIUM (60). Release decision: Caution.",
  "analysis": {
    "traceabilityId": "PROJ-4",
    "contractVersion": "v2",
    "analysisSummary": "Requirement status: READY. Automation: Hybrid (UI + API). Risk: MEDIUM (60). Release decision: Caution.",
    "stages": {
      "requirement": {
        "status": "READY",
        "featureSummary": "Apply a coupon code during checkout to receive a discount on the order total.",
        "clarifiedRequirements": ["Valid coupon applies discount to subtotal before tax", "..."],
        "edgeCases": ["Expired coupon code", "Empty coupon field", "..."],
        "openQuestions": ["What is the format of a valid coupon code?", "..."]
      },
      "testDesign": {
        "testScenarios": ["Valid Coupon Application", "Invalid Coupon", "..."],
        "testCases": [
          {
            "id": "TC-01",
            "title": "Apply Valid Coupon Code",
            "steps": ["Enter valid coupon", "Click apply", "Verify discount"],
            "expectedResult": "Discount applied to subtotal",
            "testType": "UI",
            "suiteTag": "Smoke",
            "priority": "High"
          }
        ]
      },
      "automation": {
        "automationRecommendation": "Automation (UI-heavy)",
        "coverageSplit": "UI 100% / API 0%",
        "frameworkSuggestion": "Selenium + TestNG (Java) or Playwright (TypeScript)"
      },
      "risk": {
        "riskScore": 60,
        "riskLevel": "MEDIUM",
        "releaseRecommendation": "Caution"
      },
      "bugReport": {
        "title": "Coupon Code Application Failure During Checkout",
        "severity": "Medium",
        "priority": "P3",
        "reproductionSteps": ["Enter valid coupon", "Click apply", "Verify result"],
        "expectedResult": "Discount applied correctly",
        "actualResult": "To be filled by QA engineer after test execution.",
        "suggestedAssignee": "Backend Developer"
      }
    }
  }
}
```

---

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/qa/health` | Health check |
| POST | `/qa/api/v1/qa/analyze` | Primary endpoint |
| POST | `/qa/analyze` | Compatibility alias |
| POST | `/qa/run/{issueKey}` | Legacy path endpoint |

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3, Maven |
| LLM | Groq API (Llama 3.3 70B) |
| Infrastructure | Docker, Render |
| Integrations | Jira REST API, Microsoft Copilot Studio, Power Automate |

---

## Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `JIRA_BASE_URL` | Yes | Jira instance URL |
| `JIRA_EMAIL` | Yes | Jira account email |
| `JIRA_API_TOKEN` | Yes | Jira API token |
| `GROQ_API_KEY` | Yes | Groq API key |

---

## Local Development

```bash
export JIRA_BASE_URL=https://your-domain.atlassian.net
export JIRA_EMAIL=your-email@example.com
export JIRA_API_TOKEN=your-jira-api-token
export GROQ_API_KEY=gsk_...

./mvnw spring-boot:run
```

Test:

```bash
curl -X POST http://localhost:10000/qa/api/v1/qa/analyze \
-H "Content-Type: application/json" \
-d '{"issueKey":"PROJ-4"}'
```

---

## API Contract

Current version: `v2`

Primary structured output: `analysis.stages`

Top-level fields (`analysis.requirementStatus`, `analysis.riskLevel`, etc.) are retained for backward compatibility with earlier integrations.

---

## Security

- Never commit API keys or credentials to the repository
- Use environment variables or a secure secret manager
- Keep production credentials outside the codebase

---

## Roadmap

| Phase | Status | Description |
|-------|--------|-------------|
| Phase 1 | ✅ Complete | Spring Boot backend, Jira integration, multi-stage pipeline, Render deployment |
| Phase 2 | ✅ Complete | LLM-powered stages, Groq integration, versioned API contract, Copilot Studio integration |
| Phase 3 | ✅ Complete | Structured logging, health endpoint, error handling, retry logic, observability |
| Phase 4 | 🔄 In Progress | Input validation, pipeline hardening, coverage tracking |
| Phase 5 | 📋 Planned | Historical bug analysis, coverage-based risk scoring, QA insights dashboard |