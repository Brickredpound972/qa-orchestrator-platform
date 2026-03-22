# QA Orchestrator Platform

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/SpringBoot-3-green)
![API](https://img.shields.io/badge/API-Live-brightgreen)
![Contract](https://img.shields.io/badge/Contract-v2-orange)
![LLM](https://img.shields.io/badge/LLM-Groq%20%2F%20Llama%203.3-purple)
![DB](https://img.shields.io/badge/DB-PostgreSQL-blue)

**QA Orchestrator Platform** is an AI-powered QA decision engine that covers the full QA lifecycle — from ticket creation to release.

When a developer moves a ticket to **"In Progress"**, the system automatically runs a full QA analysis pipeline. When the ticket moves to **"Done"**, the system generates a final QA release summary. Everything is tracked in PostgreSQL and visualized on a live dashboard.

---

## Live

| | URL |
|---|---|
| API | https://qa-orchestrator-service.onrender.com |
| Dashboard | https://qa-orchestrator-service.onrender.com/qa/dashboard |
| Health | https://qa-orchestrator-service.onrender.com/qa/health |

---

## Full QA Lifecycle

```
Developer opens Jira ticket
        ↓
Developer moves ticket to "In Progress"
        ↓
Jira webhook fires → QA pipeline runs automatically
        ↓
Stage 1 — Requirement Analysis
Stage 2 — Test Design
Stage 3 — Automation Decision
Stage 4 — Risk Analysis
Stage 5 — Bug Report Template
        ↓
Results saved to PostgreSQL + added as Jira comment
        ↓
Dashboard updated — risk level, test cases, release decision
        ↓
Developer moves ticket to "Done"
        ↓
Jira webhook fires → QA Release Summary generated
        ↓
Verdict: APPROVED / APPROVED WITH RISK / RELEASED WITHOUT FULL COVERAGE
        ↓
Summary saved to PostgreSQL + added as Jira comment
        ↓
Dashboard — Released tickets section updated
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

All stages are LLM-powered and feed into each other.

| Stage | Input | Output |
|-------|-------|--------|
| Requirement Analysis | Raw Jira JSON | clarifiedRequirements, edgeCases, openQuestions, scope |
| Test Design | Requirement output | testScenarios, testCases |
| Automation Decision | Test distribution + risk | automationRecommendation, coverageSplit, framework |
| Risk Analysis | All previous stages | riskScore, riskLevel, releaseRecommendation |
| Bug Report | Full pipeline context | bug report template |
| Release Summary | Analysis history from DB | APPROVED / APPROVED WITH RISK / RELEASED WITHOUT FULL COVERAGE |

---

## Intelligence Dashboard

Available at `/qa/dashboard`:

- Total analyses, avg risk score, blocked releases, released tickets
- Risk distribution chart (High / Medium / Low) — real DB counts
- Release decision chart (Block / Caution / Go) — real DB counts
- Recent analyses table with risk badges
- Blocked tickets list
- **Released tickets — QA verdicts with release summary**

---

## Jira Webhook

| Status | Action |
|--------|--------|
| `In Progress` | Triggers full QA analysis pipeline |
| `Done` | Triggers QA release summary generation |

**Setup:**
1. Jira → System → WebHooks → Create a WebHook
2. URL: `https://qa-orchestrator-service.onrender.com/qa/webhook/jira`
3. Event: `Issue updated`
4. JQL: `project = PROJ`

---

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/qa/health` | Health check with intelligence summary |
| GET | `/qa/dashboard` | QA Intelligence Dashboard |
| POST | `/qa/api/v1/qa/analyze` | Manual analysis trigger |
| POST | `/qa/webhook/jira` | Jira webhook receiver |
| GET | `/qa/api/v1/history` | Last 10 analyses |
| GET | `/qa/api/v1/history/{issueKey}` | History for specific ticket |
| GET | `/qa/api/v1/intelligence/summary` | Aggregated intelligence summary |
| GET | `/qa/api/v1/intelligence/high-risk` | All HIGH risk analyses |
| GET | `/qa/api/v1/intelligence/blocked` | All blocked analyses |
| GET | `/qa/api/v1/intelligence/released` | All released tickets with QA summaries |

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
| Phase 1 | ✅ Complete | Spring Boot backend, Jira integration, pipeline, Render deployment |
| Phase 2 | ✅ Complete | LLM-powered stages, Groq integration, versioned API contract |
| Phase 3 | ✅ Complete | Structured logging, health endpoint, error handling, retry logic |
| Phase 4 | ✅ Complete | Input validation, timeout protection, Jira error handling, logging cleanup |
| Phase 5 | ✅ Complete | PostgreSQL persistence, history API, intelligence endpoints, dashboard |
| Phase 6 | ✅ Complete | Full QA lifecycle — webhook automation, release summary, LLM provider abstraction |
| Phase 7 | 📋 Planned | Coverage tracking, risk trends, historical bug analysis, multi-tenant support |