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
- application.yml cleanup
- Render sleep prevention (cron-job on /qa/health)

## Phase 4 — Hardening & Quality 🔄 In Progress

- Input validation (issueKey format enforcement)
- Request/response sanitization
- Improved Jira error messages
- Pipeline timeout protection
- Coverage tracking foundations

## Phase 5 — QA Intelligence Layer (Planned)

- Historical bug analysis
- Coverage-based risk scoring
- Release decision engine
- QA insights dashboard