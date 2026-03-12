## Environment Setup

This project uses environment variables for Jira credentials and configuration.

Required variables:

- `JIRA_BASE_URL`
- `JIRA_EMAIL`
- `JIRA_API_TOKEN`

Example:

```env
JIRA_BASE_URL=https://your-domain.atlassian.net
JIRA_EMAIL=your-email@example.com
JIRA_API_TOKEN=your-jira-api-token

macOS / zsh

Export the variables before starting the application:
export JIRA_BASE_URL=https://your-domain.atlassian.net
export JIRA_EMAIL=your-email@example.com
export JIRA_API_TOKEN=your-jira-api-token

Then run the application:
./mvnw spring-boot:run

Notes
	•	Do not store real credentials in the repository.
	•	Do not commit .env, application-local.yml, or any file containing secrets.
	•	Use .env.example only as a reference template.