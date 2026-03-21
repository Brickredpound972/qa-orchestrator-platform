package com.qa.qa_orchestrator_service.service.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * ClaudeClient
 *
 * Thin HTTP client for the Anthropic Messages API.
 * Each QA stage calls this with its own system prompt and user content.
 * Returns the raw text response from Claude.
 */
@Component
public class ClaudeClient {

    private static final String ANTHROPIC_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-sonnet-4-5";
    private static final String ANTHROPIC_VERSION = "2023-06-01";

    @Value("${anthropic.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Send a prompt to Claude and return the text response.
     *
     * @param systemPrompt  The stage-specific system instruction
     * @param userContent   The Jira issue content or stage input
     * @return              Raw text response from Claude
     */
    public String call(String systemPrompt, String userContent) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", ANTHROPIC_VERSION);

        Map<String, Object> message = Map.of(
                "role", "user",
                "content", userContent
        );

        Map<String, Object> requestBody = Map.of(
                "model", MODEL,
                "max_tokens", 2048,
                "system", systemPrompt,
                "messages", List.of(message)
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    ANTHROPIC_API_URL,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            return extractTextFromResponse(response.getBody());

        } catch (Exception e) {
            throw new RuntimeException("Claude API call failed: " + e.getMessage(), e);
        }
    }

    /**
     * Parse the Anthropic response envelope and return the text content.
     */
    private String extractTextFromResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode content = root.path("content");

            if (content.isArray() && content.size() > 0) {
                JsonNode firstBlock = content.get(0);
                if ("text".equals(firstBlock.path("type").asText())) {
                    return firstBlock.path("text").asText();
                }
            }

            throw new RuntimeException("Unexpected Claude response structure: " + responseBody);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Claude response: " + e.getMessage(), e);
        }
    }
}