# QA Orchestrator Platform — Roadmap

## Phase 1 — Foundation ✅ Complete

- Spring Boot backend
- Jira issue retrieval via REST API
- Multi-stage QA pipeline (Requirement, TestDesign, Automation, Risk)
- Render cloud deployment
- Copilot Studio integration
- Versioned API contract (v2)

---

## Phase 2 — LLM Intelligence ✅ Complete

- LLM-powered pipeline (Groq API — Llama 3.3 70B)
- RequirementAnalysisStage — LLM-powered
- TestDesignStage — LLM-powered, feeds from RequirementStage
- AutomationDecisionStage — LLM-powered, reads test case type distribution
- RiskAnalysisStage — LLM-powered, reads full pipeline context
- BugReportStage — LLM-powered, generates pre-filled bug report templates
- Pipeline refactor — stages feed each other, no keyword matching
- Controller fix — pipeline runs once per request
- README, architecture, API contract updated

---

## Phase 3 — QA Intelligence Layer 🔄 Planned

- QA Context Service — historical ticket awareness
- Historical bug analysis — bug density per component
- Coverage-based risk scoring — risk adjusted by existing test coverage
- Release decision engine — structured go/no-go logic
- Structured error responses — standardized error contract
- Logging and observability — request tracing, stage timing

---

## Phase 4 — Extended QA Lifecycle 🔄 Planned

- Test execution planning stage
- Coverage tracking
- QA insights dashboard
- Smarter requirement ambiguity detection
- Expanded release decision logic