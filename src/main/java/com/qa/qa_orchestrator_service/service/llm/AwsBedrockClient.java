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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

/**
 * AwsBedrockClient
 *
 * LLM provider: AWS Bedrock (Claude 3.5 Sonnet)
 * Active when: LLM_PROVIDER=aws
 *
 * Required env vars:
 * - LLM_PROVIDER=aws
 * - AWS_ACCESS_KEY=your-access-key
 * - AWS_SECRET_KEY=your-secret-key
 * - AWS_REGION=us-east-1
 */
@Component
@ConditionalOnProperty(name = "llm.provider", havingValue = "aws")
public class AwsBedrockClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(AwsBedrockClient.class);
    private static final String MODEL_ID = "anthropic.claude-3-5-sonnet-20241022-v2:0";
    private static final String SERVICE = "bedrock";

    @Value("${aws.access-key}")
    private String accessKey;

    @Value("${aws.secret-key}")
    private String secretKey;

    @Value("${aws.region:us-east-1}")
    private String region;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AwsBedrockClient(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Override
    public String call(String systemPrompt, String userContent) {
        try {
            String url = String.format(
                "https://bedrock-runtime.%s.amazonaws.com/model/%s/invoke",
                region, MODEL_ID);

            Map<String, Object> requestBody = Map.of(
                    "anthropic_version", "bedrock-2023-05-31",
                    "max_tokens", 2048,
                    "system", systemPrompt,
                    "messages", List.of(
                            Map.of("role", "user", "content", userContent)
                    )
            );

            String body = objectMapper.writeValueAsString(requestBody);
            HttpHeaders headers = buildAwsHeaders(url, body);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST,
                    new HttpEntity<>(body, headers), String.class);

            return extractText(response.getBody());

        } catch (Exception e) {
            log.error("[AWS] Bedrock call failed: {}", e.getMessage());
            throw new RuntimeException("AWS Bedrock call failed: " + e.getMessage(), e);
        }
    }

    private String extractText(String responseBody) {
        try {
            return objectMapper.readTree(responseBody)
                    .path("content").get(0)
                    .path("text").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AWS Bedrock response: " + e.getMessage(), e);
        }
    }

    private HttpHeaders buildAwsHeaders(String url, String body) throws Exception {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        String amzDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"));
        String dateStamp = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String host = url.replaceAll("https://", "").split("/")[0];
        String bodyHash = sha256Hex(body);

        String canonicalHeaders = "content-type:application/json\nhost:" + host + "\nx-amz-date:" + amzDate + "\n";
        String signedHeaders = "content-type;host;x-amz-date";
        String path = "/" + url.split(host)[1].substring(1);

        String canonicalRequest = "POST\n" + path + "\n\n" + canonicalHeaders + "\n" + signedHeaders + "\n" + bodyHash;
        String credentialScope = dateStamp + "/" + region + "/" + SERVICE + "/aws4_request";
        String stringToSign = "AWS4-HMAC-SHA256\n" + amzDate + "\n" + credentialScope + "\n" + sha256Hex(canonicalRequest);

        byte[] signingKey = getSigningKey(secretKey, dateStamp, region, SERVICE);
        String signature = HexFormat.of().formatHex(hmacSha256(stringToSign, signingKey));

        String authorization = "AWS4-HMAC-SHA256 Credential=" + accessKey + "/" + credentialScope
                + ", SignedHeaders=" + signedHeaders + ", Signature=" + signature;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-amz-date", amzDate);
        headers.set("Authorization", authorization);
        return headers;
    }

    private byte[] getSigningKey(String key, String date, String region, String service) throws Exception {
        byte[] kDate = hmacSha256(date, ("AWS4" + key).getBytes(StandardCharsets.UTF_8));
        byte[] kRegion = hmacSha256(region, kDate);
        byte[] kService = hmacSha256(service, kRegion);
        return hmacSha256("aws4_request", kService);
    }

    private byte[] hmacSha256(String data, byte[] key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private String sha256Hex(String data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(digest.digest(data.getBytes(StandardCharsets.UTF_8)));
    }
}