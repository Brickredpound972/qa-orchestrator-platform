# QA Orchestrator Platform

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/SpringBoot-3-green)
![API](https://img.shields.io/badge/API-Live-brightgreen)
![Contract](https://img.shields.io/badge/Contract-v3-orange)
![LLM](https://img.shields.io/badge/LLM-Groq%20%2F%20Llama%203.3-purple)
![DB](https://img.shields.io/badge/DB-PostgreSQL-blue)
![Copilot](https://img.shields.io/badge/Copilot-Studio-0078d4)

**QA Orchestrator Platform** is an AI-powered QA decision engine that covers the full QA lifecycle — from ticket creation to release — integrated with Microsoft Copilot Studio and Power Automate.

---

## Live

| | URL |
|---|---|
| Dashboard | https://qa-orchestrator-service.onrender.com |
| API | https://qa-orchestrator-service.onrender.com/qa/api/v1/qa/analyze |
| Health | https://qa-orchestrator-service.onrender.com/qa/health |

---

## Full QA Lifecycle

```
Developer opens Jira ticket
        ↓
Moves ticket to "In Progress"
        ↓
Jira webhook fires → QA pipeline runs automatically
        ↓
Stage 1 — Requirement Analysis
Stage 2 — Test Design
Stage 3 — Automation Decision
Stage 4 — Risk Analysis
Stage 5 — Bug Report Template
        ↓
Results → PostgreSQL + Jira comment + Dashboard
        ↓
Moves ticket to "Done"
        ↓
Jira webhook fires → QA Release Summary generated
        ↓
Verdict: APPROVED / APPROVED WITH RISK / RELEASED WITHOUT FULL COVERAGE
        ↓
Summary → PostgreSQL + Jira comment + Dashboard
```

---

## System Architecture

```
Jira (webhook) / Copilot Studio / Power Automate
            │
            ▼
    QA Orchestrator API
            │
            ▼
     Spring Boot Service
       │           │           │
       ▼           ▼           ▼
  Jira REST    LLM API     PostgreSQL
  (tickets)  (Groq/Azure    (history &
              /AWS)         intelligence)
```

---

## LLM Provider Support

Switch providers by changing a single env var — no code change needed.

| Provider | Env Var | Model | Best For |
|----------|---------|-------|----------|
| Groq (default) | `LLM_PROVIDER=groq` | Llama 3.3 70B | Development, free tier |
| Azure OpenAI | `LLM_PROVIDER=azure` | GPT-4o | Enterprise, Microsoft ecosystem |
| AWS Bedrock | `LLM_PROVIDER=aws` | Claude 3.5 Sonnet | Enterprise, AWS ecosystem |

---

## QA Analysis Pipeline

| Stage | Input | Output |
|-------|-------|--------|
| Requirement Analysis | Raw Jira JSON | clarifiedRequirements, edgeCases, openQuestions, scope |
| Test Design | Requirement output | testScenarios, testCases |
| Automation Decision | Test distribution + risk | automationRecommendation, coverageSplit, framework |
| Risk Analysis | All previous stages | riskScore, riskLevel, releaseRecommendation |
| Bug Report | Full pipeline context | bug report template |
| Release Summary | Analysis history from DB | APPROVED / APPROVED WITH RISK / RELEASED WITHOUT FULL COVERAGE |

---

## Copilot Studio Topics

| Topic | Trigger | Action |
|-------|---------|--------|
| `agent_requirement_analyzer` | "Analyze PROJ-4" | `analyzeQaIssue` |
| `agent_test_case_generator` | "Generate test cases" | `analyzeQaIssue` |
| `agent_risk_predictor` | "What is the risk?" | `analyzeQaIssue` |
| `agent_automation_builder` | "Automation strategy" | `analyzeQaIssue` |
| `agent_bug_reporter` | "Create bug report" | `analyzeQaIssue` |
| `agent_intelligence_summary` | "QA summary / risk status" | `getIntelligenceSummary` |
| `agent_release_summary` | "Released tickets / verdict" | `getReleasedTickets` |

---

## Intelligence Dashboard

Available at root URL — `https://qa-orchestrator-service.onrender.com`:

- Total analyses, avg risk score, blocked releases, released tickets
- Risk distribution chart (High / Medium / Low)
- Release decision chart (Block / Caution / Go)
- Recent analyses table
- Blocked tickets list
- Released tickets with QA verdicts

---

## Jira Webhook

| Status | Action |
|--------|--------|
| `In Progress` | Full QA analysis pipeline |
| `Done` | QA release summary generation |

**Setup:**
1. Jira → System → WebHooks → Create a WebHook
2. URL: `https://qa-orchestrator-service.onrender.com/qa/webhook/jira`
3. Event: `Issue updated`
4. JQL: `project = PROJ`

---

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/` | Redirects to dashboard |
| GET | `/qa/health` | Health check with intelligence summary |
| GET | `/qa/dashboard` | QA Intelligence Dashboard |
| POST | `/qa/api/v1/qa/analyze` | Manual analysis trigger |
| POST | `/qa/webhook/jira` | Jira webhook receiver |
| GET | `/qa/api/v1/history` | Last 10 analyses |
| GET | `/qa/api/v1/history/{issueKey}` | History for specific ticket |
| GET | `/qa/api/v1/intelligence/summary` | Aggregated intelligence summary |
| GET | `/qa/api/v1/intelligence/high-risk` | HIGH risk analyses |
| GET | `/qa/api/v1/intelligence/blocked` | Blocked analyses |
| GET | `/qa/api/v1/intelligence/released` | Released tickets with QA summaries |

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3, Maven |
| LLM | Groq (default) / Azure OpenAI / AWS Bedrock |
| Database | PostgreSQL (Render) |
| Infrastructure | Docker, Render |
| Integrations | Jira REST API, Jira Webhooks, Microsoft Copilot Studio, Power Automate |

---

## Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `JIRA_BASE_URL` | Yes | Jira instance URL |
| `JIRA_EMAIL` | Yes | Jira account email |
| `JIRA_API_TOKEN` | Yes | Jira API token |
| `GROQ_API_KEY` | Yes (if Groq) | Groq API key |
| `AZURE_OPENAI_KEY` | Yes (if Azure) | Azure OpenAI key |
| `AZURE_OPENAI_ENDPOINT` | Yes (if Azure) | Azure OpenAI endpoint |
| `AZURE_OPENAI_DEPLOYMENT` | No | Model deployment (default: gpt-4o) |
| `AWS_ACCESS_KEY` | Yes (if AWS) | AWS access key |
| `AWS_SECRET_KEY` | Yes (if AWS) | AWS secret key |
| `AWS_REGION` | No | AWS region (default: us-east-1) |
| `LLM_PROVIDER` | No | groq / azure / aws (default: groq) |
| `SPRING_DATASOURCE_URL` | Yes | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | Yes | PostgreSQL username |
| `SPRING_DATASOURCE_PASSWORD` | Yes | PostgreSQL password |
| `JIRA_COMMENT_ENABLED` | No | Write analysis as Jira comment (default: false) |

---

## Roadmap

| Phase | Status | Description |
|-------|--------|-------------|
| Phase 1 | ✅ | Spring Boot backend, Jira integration, pipeline, Render deployment |
| Phase 2 | ✅ | LLM-powered stages, Groq integration, versioned API contract |
| Phase 3 | ✅ | Structured logging, health endpoint, error handling, retry logic |
| Phase 4 | ✅ | Input validation, timeout protection, Jira error handling, logging cleanup |
| Phase 5 | ✅ | PostgreSQL, history API, intelligence endpoints, dashboard |
| Phase 6 | ✅ | Full QA lifecycle — webhook automation, release summary, LLM provider abstraction |
| Phase 7 | ✅ | Copilot Studio v2 — intelligence summary + release summary topics |
| Phase 8 | 📋 | Dashboard filtering, risk trends, coverage tracking, multi-tenant |