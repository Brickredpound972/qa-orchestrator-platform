package com.qa.qa_orchestrator_service.util;

/**
 * IssueKeyNormalizer
 *
 * Normalizes Jira issue key input from various formats to standard Jira format.
 *
 * Examples:
 *   "project-5"   → "PROJECT-5"
 *   "proj 5"      → "PROJ-5"
 *   "PROJ-5"      → "PROJ-5"
 *   "proj5"       → "PROJ-5"
 *   "Project 5"   → "PROJECT-5"
 */
public class IssueKeyNormalizer {

    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) return raw;

        String upper = raw.trim().toUpperCase();
        upper = upper.replaceAll("\\s+", "-");
        upper = upper.replaceAll("([A-Z])([0-9])", "$1-$2");
        upper = upper.replaceAll("[^A-Z0-9\\-]", "");
        upper = upper.replaceAll("-{2,}", "-");

        return upper;
    }
}