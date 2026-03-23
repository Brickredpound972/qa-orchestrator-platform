# QA Orchestrator Platform — Roadmap

## Phase 1 — Foundation ✅ Complete
- Spring Boot backend, Jira integration, pipeline, Render deployment, Copilot Studio, API contract

## Phase 2 — LLM Intelligence ✅ Complete
- Groq LLM integration, all 5 stages LLM-powered, bug report generation, retry logic

## Phase 3 — Observability & Reliability ✅ Complete
- Structured logging, health endpoint, error handling, Groq retry, Render sleep prevention

## Phase 4 — Hardening & Quality ✅ Complete
- Input validation, timeouts, Jira error handling, SLF4J logging, JPA open-in-view disabled

## Phase 5 — QA Intelligence Layer ✅ Complete
- PostgreSQL, history API, intelligence endpoints, dashboard, root URL redirect

## Phase 6 — Full QA Lifecycle Automation ✅ Complete
- Jira webhook (In Progress → analysis, Done → release summary)
- LlmClient interface — Groq / Azure OpenAI / AWS Bedrock
- Released tickets dashboard section with QA verdict

## Phase 7 — Copilot Studio v2 ✅ Complete
- Swagger v3.0.0 — all endpoints documented
- agent_intelligence_summary — QA status in Teams
- agent_release_summary — release verdicts in Teams
- Power Automate connector updated to v3

## Phase 8 — Dashboard Intelligence ✅ Complete
- Search across all tables (issue key, feature summary)
- Filter by risk level (High / Medium / Low)
- Filter by release recommendation (Block / Caution / Go)
- Record count badges per section

## Phase 9 — Multi-tenant Support 📋 Planned
- Per-customer database isolation
- Customer onboarding flow
- Separate Jira credentials per tenant
- Usage tracking per tenant

## Phase 10 — Advanced Intelligence 📋 Planned
- Risk trend analysis — how risk evolves over time per ticket
- Coverage tracking across test suites
- Historical bug analysis for smarter risk scoring
- Predictive risk scoring based on past patterns

## Phase 11 — Production Hardening 📋 Planned
- Azure / AWS deployment option
- SOC2 compliance preparation
- Rate limiting per tenant
- Audit logging