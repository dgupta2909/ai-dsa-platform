package com.aidsa.platform.dto;

public record RecommendationResponse(
        ProblemSummaryResponse problem,
        double score,
        String reason,
        String priority
) {
}