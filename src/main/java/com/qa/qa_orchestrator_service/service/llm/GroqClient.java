package com.qa.qa_orchestrator_service.service.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * GroqClient
 *
 * LLM client using Groq API with automatic retry on rate limit (429).
 * Groq free tier has per-minute token limits — retry logic handles this gracefully.
 */
@Component
public class GroqClient {

    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.3-70b-versatile";
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 10000; // 10 seconds

    @Value("${groq.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String call(String systemPrompt, String userContent) {
        int attempts = 0;

        while (attempts < MAX_RETRIES) {
            try {
                return doCall(systemPrompt, userContent);

            } catch (HttpClientErrorException e) {
                if (e.getStatusCode().value() == 429) {
                    attempts++;
                    if (attempts < MAX_RETRIES) {
                        System.out.println("[GROQ] Rate limit hit, retrying in " +
                                (RETRY_DELAY_MS / 1000) + "s... (attempt " + attempts + "/" + MAX_RETRIES + ")");
                        try {
                            Thread.sleep(RETRY_DELAY_MS);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("Retry interrupted", ie);
                        }
                    } else {
                        throw new RuntimeException("Groq rate limit exceeded after " + MAX_RETRIES + " retries: " + e.getMessage(), e);
                    }
                } else {
                    throw new RuntimeException("Groq API call failed: " + e.getMessage(), e);
                }
            } catch (Exception e) {
                throw new RuntimeException("Groq API call failed: " + e.getMessage(), e);
            }
        }

        throw new RuntimeException("Groq API call failed after " + MAX_RETRIES + " retries");
    }

    private String doCall(String systemPrompt, String userContent) {
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

        ResponseEntity<String> response = restTemplate.exchange(
                GROQ_API_URL,
                HttpMethod.POST,
                entity,
                String.class
        );

        return extractTextFromResponse(response.getBody());
    }

    private String extractTextFromResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.path("choices");

            if (choices.isArray() && choices.size() > 0) {
                return choices.get(0).path("message").path("content").asText();
            }

            throw new RuntimeException("Unexpected Groq response structure: " + responseBody);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Groq response: " + e.getMessage(), e);
        }
    }
}