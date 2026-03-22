# QA Orchestrator Platform

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/SpringBoot-3-green)
![API](https://img.shields.io/badge/API-Live-brightgreen)
![Contract](https://img.shields.io/badge/Contract-v2-orange)
![LLM](https://img.shields.io/badge/LLM-Groq%20%2F%20Llama%203.3-purple)
![DB](https://img.shields.io/badge/DB-PostgreSQL-blue)

**QA Orchestrator Platform** is an AI-powered QA decision engine that analyzes Jira issues and produces structured QA intelligence — including requirement analysis, test cases, automation strategy, risk scoring, bug report templates, and historical intelligence.

---

## Live

| | URL |
|---|---|
| API | https://qa-orchestrator-service.onrender.com |
| Dashboard | https://qa-orchestrator-service.onrender.com/qa/dashboard |
| Health | https://qa-orchestrator-service.onrender.com/qa/health |

---

## What It Does

```
Developer writes Jira ticket
        ↓
QA Orchestrator reads the ticket
        ↓
Stage 1 — Requirement Analysis
Stage 2 — Test Design
Stage 3 — Automation Decision
Stage 4 — Risk Analysis
Stage 5 — Bug Report Template
        ↓
Results saved to PostgreSQL
        ↓
Intelligence Dashboard updated
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
       │           │           │
       ▼           ▼           ▼
  Jira REST    Groq LLM    PostgreSQL
```

---

## QA Analysis Pipeline

All 5 stages are LLM-powered. Each stage reads from the previous stage output.

| Stage | Input | Output |
|-------|-------|--------|
| Requirement Analysis | Raw Jira JSON | clarifiedRequirements, edgeCases, openQuestions, scope |
| Test Design | Requirement stage output | testScenarios, testCases |
| Automation Decision | Test case distribution + risk | automationRecommendation, coverageSplit, framework |
| Risk Analysis | All previous stages | riskScore, riskLevel, releaseRecommendation |
| Bug Report | Full pipeline context | bug report template ready for QA engineer |

---

## Intelligence Dashboard

Available at `/qa/dashboard` — a live browser-accessible dashboard showing:

- Total analyses, average risk score, high risk count, blocked releases
- Risk distribution chart (High / Medium / Low)
- Release decision chart (Block / Caution / Go)
- Recent analyses table with risk badges
- Blocked tickets list with automation strategy

---

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/qa/health` | Health check with intelligence summary |
| GET | `/qa/dashboard` | QA Intelligence Dashboard |
| POST | `/qa/api/v1/qa/analyze` | Primary analysis endpoint |
| POST | `/qa/analyze` | Compatibility alias |
| POST | `/qa/run/{issueKey}` | Legacy path |
| GET | `/qa/api/v1/history` | Last 10 analyses |
| GET | `/qa/api/v1/history/{issueKey}` | History for specific ticket |
| GET | `/qa/api/v1/intelligence/summary` | Aggregated intelligence summary |
| GET | `/qa/api/v1/intelligence/high-risk` | All HIGH risk analyses |
| GET | `/qa/api/v1/intelligence/blocked` | All blocked analyses |

---

## Example Request

```bash
curl -X POST https://qa-orchestrator-service.onrender.com/qa/api/v1/qa/analyze \
-H "Content-Type: application/json" \
-d '{"issueKey":"PROJ-4"}'
```

---

## Input Validation

The `issueKey` field must follow Jira format: `PROJECT-NUMBER` (e.g. `PROJ-4`, `QA-123`).

Invalid input returns:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "issueKey must follow Jira format: PROJECT-NUMBER (e.g. PROJ-4)"
}
```

---

## Error Responses

| Status | Scenario |
|--------|----------|
| 400 | Invalid issueKey format |
| 404 | Jira issue not found |
| 401 | Jira authentication failed |
| 504 | Pipeline timed out |
| 500 | Unexpected error |

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3, Maven |
| LLM | Groq API (Llama 3.3 70B) |
| Database | PostgreSQL (Render) |
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
| `SPRING_DATASOURCE_URL` | Yes | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | Yes | PostgreSQL username |
| `SPRING_DATASOURCE_PASSWORD` | Yes | PostgreSQL password |
| `JIRA_COMMENT_ENABLED` | No | Write analysis as Jira comment (default: false) |

---

## Local Development

```bash
export JIRA_BASE_URL=https://your-domain.atlassian.net
export JIRA_EMAIL=your-email@example.com
export JIRA_API_TOKEN=your-jira-api-token
export GROQ_API_KEY=gsk_...
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/qa_orchestrator_db
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=your-password

./mvnw spring-boot:run
```

---

## Roadmap

| Phase | Status | Description |
|-------|--------|-------------|
| Phase 1 | ✅ Complete | Spring Boot backend, Jira integration, pipeline, Render deployment |
| Phase 2 | ✅ Complete | LLM-powered stages, Groq integration, versioned API contract |
| Phase 3 | ✅ Complete | Structured logging, health endpoint, error handling, retry logic |
| Phase 4 | ✅ Complete | Input validation, timeout protection, Jira error handling, logging cleanup |
| Phase 5 | ✅ Complete | PostgreSQL persistence, history API, intelligence endpoints, dashboard |
| Phase 6 | 📋 Planned | Coverage tracking, risk trends, historical bug analysis, dashboard improvements |