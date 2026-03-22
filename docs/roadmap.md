# QA Orchestrator Platform — Roadmap

## Phase 1 — Foundation ✅ Complete
- Spring Boot backend
- Jira issue retrieval
- Multi-agent QA pipeline
- Render deployment
- Copilot Studio integration
- API contract

## Phase 2 — LLM Intelligence ✅ Complete
- Groq LLM integration (llama-3.3-70b-versatile)
- LLM-powered requirement analysis
- LLM-powered test design
- LLM-powered automation decision
- LLM-powered risk analysis
- LLM-powered bug report generation
- Formatted pipeline output for Copilot Studio
- Retry logic for Groq rate limits

## Phase 3 — Observability & Reliability ✅ Complete
- Structured pipeline logging with stage timing
- Health check endpoint (`/qa/health`)
- Structured error responses (GlobalExceptionHandler)
- Groq retry mechanism (429 handling)
- Render sleep prevention (cron-job on /qa/health)

## Phase 4 — Hardening & Quality ✅ Complete
- Input validation (issueKey format enforcement)
- Groq + Jira client timeouts
- Pipeline-level timeout (25s via CompletableFuture)
- Jira 404 clean error response
- Jira comment toggle (jira.comment-enabled flag)
- SLF4J logging throughout (no System.out.println)
- JPA open-in-view disabled

## Phase 5 — QA Intelligence Layer ✅ Complete
- PostgreSQL database on Render (Free tier)
- AnalysisRecord entity — persists every pipeline result
- History endpoints — recent analyses and per-issue history
- Intelligence endpoints — summary, high-risk, blocked tickets
- Health endpoint enriched with intelligence summary
- QA Intelligence Dashboard at `/qa/dashboard`
- Real risk distribution and release decision charts

## Phase 6 — Automation & Provider Flexibility ✅ Complete
- Jira webhook endpoint (`POST /qa/webhook/jira`)
- Auto-trigger analysis when ticket moves to "In Progress"
- Zero manual steps — developer changes status, pipeline runs
- LlmClient interface — provider agnostic pipeline
- GroqClient, AzureOpenAiClient, AwsBedrockClient implementations
- Switch provider via `LLM_PROVIDER` env var (groq / azure / aws)

## Phase 7 — Extended QA Lifecycle 📋 Planned
- Coverage tracking across test suites
- Risk trend analysis — how risk evolves across re-analyses
- Historical bug analysis for smarter risk scoring
- Dashboard improvements — drill-down, filtering, search
- Multi-tenant support — per-customer isolation