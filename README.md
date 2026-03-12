QA Orchestrator Platform

AI-powered QA orchestration platform that analyzes Jira issues and generates testing strategy, risk assessment, and automation recommendations.

The platform integrates with Jira, processes issue details, and produces QA insights that help teams decide test strategy, automation scope, and release risk.

Architecture
Copilot Studio / Power Automate
        ↓
Custom Connector
        ↓
QA Orchestrator API
        ↓
Spring Boot Service
        ↓
Jira REST API

Live API

Production endpoint:

https://qa-orchestrator-service.onrender.com

⸻

Example Request
curl -X POST https://qa-orchestrator-service.onrender.com/qa/api/v1/qa/analyze \
-H "Content-Type: application/json" \
-d '{"issueKey":"PROJ-4"}'

Example Response
{
  "output": "QA Orchestrator Analysis..."
}

Tech Stack
	•	Java 17
	•	Spring Boot
	•	Maven
	•	Docker
	•	Render (Cloud Deployment)
	•	Jira REST API
	•	Microsoft Copilot Studio
	•	Power Automate

⸻

Environment Setup

This project uses environment variables for Jira credentials and configuration.

Required variables:
	•	JIRA_BASE_URL
	•	JIRA_EMAIL
	•	JIRA_API_TOKEN

Example:
Tech Stack
	•	Java 17
	•	Spring Boot
	•	Maven
	•	Docker
	•	Render (Cloud Deployment)
	•	Jira REST API
	•	Microsoft Copilot Studio
	•	Power Automate

⸻

Environment Setup

This project uses environment variables for Jira credentials and configuration.

Required variables:
	•	JIRA_BASE_URL
	•	JIRA_EMAIL
	•	JIRA_API_TOKEN

Example:
JIRA_BASE_URL=https://your-domain.atlassian.net
JIRA_EMAIL=your-email@example.com
JIRA_API_TOKEN=your-jira-api-token

Local Development

Export the environment variables before starting the application.

macOS / zsh
export JIRA_BASE_URL=https://your-domain.atlassian.net
export JIRA_EMAIL=your-email@example.com
export JIRA_API_TOKEN=your-jira-api-token

Run the application:
./mvnw spring-boot:run

The API will start locally and can be tested via curl or Postman.

⸻

Security Notes
	•	Do not store real credentials in the repository.
	•	Do not commit .env, application-local.yml, or any file containing secrets.
	•	Use .env.example only as a reference template.

⸻

Project Goal

QA Orchestrator Platform aims to support modern QA teams by:
	•	Analyzing Jira requirements
	•	Generating QA strategy recommendations
	•	Identifying testing risks
	•	Suggesting automation approach (UI / API / Hybrid)

This helps teams move faster while maintaining release quality.