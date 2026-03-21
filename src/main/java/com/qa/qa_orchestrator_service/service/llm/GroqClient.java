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
 * GroqClient
 *
 * LLM client using Groq API (OpenAI-compatible format).
 * Drop-in replacement for ClaudeClient during development.
 * Switch to Claude API in production by swapping this component.
 */
@Component
public class GroqClient {

    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.3-70b-versatile";

    @Value("${groq.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Send a prompt to Groq and return the text response.
     *
     * @param systemPrompt  Stage-specific system instruction
     * @param userContent   Jira issue content or stage input
     * @return              Raw text response from the model
     */
    public String call(String systemPrompt, String userContent) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> systemMessage = Map.of(
                "role", "system",
                "content", systemPrompt
        );

        Map<String, Object> userMessage = Map.of(
                "role", "user",
                "content", userContent
        );

        Map<String, Object> requestBody = Map.of(
                "model", MODEL,
                "messages", List.of(systemMessage, userMessage),
                "max_tokens", 2048,
                "temperature", 0.2
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    GROQ_API_URL,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            return extractTextFromResponse(response.getBody());

        } catch (Exception e) {
            throw new RuntimeException("Groq API call failed: " + e.getMessage(), e);
        }
    }

    /**
     * Parse OpenAI-compatible response and extract text content.
     */
    private String extractTextFromResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.path("choices");

            if (choices.isArray() && choices.size() > 0) {
                JsonNode firstChoice = choices.get(0);
                return firstChoice.path("message").path("content").asText();
            }

            throw new RuntimeException("Unexpected Groq response structure: " + responseBody);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Groq response: " + e.getMessage(), e);
        }
    }
}