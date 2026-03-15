package com.qa.qa_orchestrator_service.jira;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import java.util.List;
import java.util.Map;

@Component
public class JiraClient {

        @Value("${jira.base-url}")
        private String baseUrl;

        @Value("${jira.email}")
        private String email;

        @Value("${jira.api-token}")
        private String token;

        private final RestTemplate restTemplate = new RestTemplate();

        public String getIssue(String issueKey) {

                String url = baseUrl + "/rest/api/3/issue/" + issueKey +
                                "?fields=summary,description,issuetype,priority,status";

                HttpHeaders headers = new HttpHeaders();
                headers.setBasicAuth(email, token);
                headers.setAccept(List.of(MediaType.APPLICATION_JSON));

                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

                return response.getBody();
        }

        public void addComment(String issueKey, String commentBody) {

                String url = baseUrl + "/rest/api/3/issue/" + issueKey + "/comment";

                HttpHeaders headers = new HttpHeaders();
                headers.setBasicAuth(email, token);
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setAccept(List.of(MediaType.APPLICATION_JSON));

                Map<String, Object> textNode = Map.of(
                                "type", "text",
                                "text", commentBody);

                Map<String, Object> paragraphNode = Map.of(
                                "type", "paragraph",
                                "content", List.of(textNode));

                Map<String, Object> bodyNode = Map.of(
                                "type", "doc",
                                "version", 1,
                                "content", List.of(paragraphNode));

                Map<String, Object> payload = Map.of(
                                "body", bodyNode);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

                restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        }

}