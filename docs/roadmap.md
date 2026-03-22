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

## Phase 4 — Hardening & Quality ✅ Complete

- Input validation (issueKey format enforcement)
- Groq client timeout (30s read, 10s connect)
- Jira client timeout (15s read, 10s connect)
- Pipeline-level timeout (25s via CompletableFuture)
- Jira 404 — clean error response
- Jira comment toggle (jira.comment-enabled flag)
- System.out.println → SLF4J logging throughout
- Validated across 3 domains (Login, Coupon, Shopping Cart)

## Phase 5 — QA Intelligence Layer 📋 Planned

- Historical bug analysis
- Coverage-based risk scoring
- Release decision engine improvements
- QA insights dashboard
- Test execution planning