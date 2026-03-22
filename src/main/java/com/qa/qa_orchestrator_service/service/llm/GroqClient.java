package com.qa.qa_orchestrator_service.service.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * GroqClient
 *
 * LLM provider: Groq (Llama 3.3 70B)
 * Active when: LLM_PROVIDER=groq (default)
 */
@Component
@ConditionalOnProperty(name = "llm.provider", havingValue = "groq", matchIfMissing = true)
public class GroqClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(GroqClient.class);

    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.3-70b-versatile";
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 10000;

    @Value("${groq.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GroqClient(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Override
    public String call(String systemPrompt, String userContent) {
        int attempts = 0;

        while (attempts < MAX_RETRIES) {
            try {
                return doCall(systemPrompt, userContent);
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode().value() == 429) {
                    attempts++;
                    if (attempts < MAX_RETRIES) {
                        log.warn("[GROQ] Rate limit hit, retrying in {}s... (attempt {}/{})",
                                RETRY_DELAY_MS / 1000, attempts, MAX_RETRIES);
                        try { Thread.sleep(RETRY_DELAY_MS); }
                        catch (InterruptedException ie) { Thread.currentThread().interrupt(); throw new RuntimeException("Retry interrupted", ie); }
                    } else {
                        throw new RuntimeException("Groq rate limit exceeded after " + MAX_RETRIES + " retries", e);
                    }
                } else {
                    throw new RuntimeException("Groq API call failed: " + e.getMessage(), e);
                }
            } catch (ResourceAccessException e) {
                throw new RuntimeException("Groq API timed out after 30 seconds", e);
            }
        }
        throw new RuntimeException("Groq API failed after " + MAX_RETRIES + " retries");
    }

    private String doCall(String systemPrompt, String userContent) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = Map.of(
                "model", MODEL,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userContent)
                ),
                "max_tokens", 2048,
                "temperature", 0.2
        );

        ResponseEntity<String> response = restTemplate.exchange(
                GROQ_API_URL, HttpMethod.POST,
                new HttpEntity<>(requestBody, headers), String.class);

        return extractText(response.getBody());
    }

    private String extractText(String responseBody) {
        try {
            JsonNode root = new ObjectMapper().readTree(responseBody);
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Groq response: " + e.getMessage(), e);
        }
    }
}