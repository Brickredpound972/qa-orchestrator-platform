package com.qa.qa_orchestrator_service.service.llm;

/**
 * LlmClient
 *
 * Common interface for all LLM providers.
 * Swap providers by changing env vars — no code change needed.
 *
 * Supported providers:
 * - Groq (default)
 * - Azure OpenAI
 * - AWS Bedrock
 */
public interface LlmClient {
    String call(String systemPrompt, String userContent);
}