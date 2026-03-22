package com.qa.qa_orchestrator_service.service.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * AzureOpenAiClient
 *
 * LLM provider: Azure OpenAI (GPT-4o)
 * Active when: LLM_PROVIDER=azure
 *
 * Required env vars:
 * - LLM_PROVIDER=azure
 * - AZURE_OPENAI_KEY=your-key
 * - AZURE_OPENAI_ENDPOINT=https://your-resource.openai.azure.com
 * - AZURE_OPENAI_DEPLOYMENT=gpt-4o
 */
@Component
@ConditionalOnProperty(name = "llm.provider", havingValue = "azure")
public class AzureOpenAiClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(AzureOpenAiClient.class);
    private static final String API_VERSION = "2024-02-01";

    @Value("${azure.openai.key}")
    private String apiKey;

    @Value("${azure.openai.endpoint}")
    private String endpoint;

    @Value("${azure.openai.deployment}")
    private String deployment;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AzureOpenAiClient(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Override
    public String call(String systemPrompt, String userContent) {
        try {
            String url = String.format(
                "%s/openai/deployments/%s/chat/completions?api-version=%s",
                endpoint, deployment, API_VERSION);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            Map<String, Object> requestBody = Map.of(
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userContent)
                    ),
                    "max_tokens", 2048,
                    "temperature", 0.2
            );

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers), String.class);

            return extractText(response.getBody());

        } catch (Exception e) {
            log.error("[AZURE] API call failed: {}", e.getMessage());
            throw new RuntimeException("Azure OpenAI call failed: " + e.getMessage(), e);
        }
    }

    private String extractText(String responseBody) {
        try {
            return objectMapper.readTree(responseBody)
                    .path("choices").get(0)
                    .path("message").path("content").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Azure OpenAI response: " + e.getMessage(), e);
        }
    }
}