package com.example.springai.dto;

import java.util.List;

/**
 * DTO for structured output demonstrations with Google Gemini in Spring AI 2.0.
 */
public record CustomerInsight(
        String customerName,
        String customerPlan,
        String churnRisk, // "LOW", "MEDIUM", "HIGH"
        String summaryAnalysis,
        List<String> suggestedNextActions
) {}
