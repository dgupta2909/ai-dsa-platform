package com.aidsa.platform.dto;

import com.aidsa.platform.model.AICodeReview;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public record AICodeReviewResponse(
        Long id,
        Long submissionId,
        String overallFeedback,
        String approach,
        String timeComplexity,
        String spaceComplexity,
        Integer codeQualityScore,
        List<String> strengths,
        List<String> improvements,
        String aiProvider,
        String aiModel,
        Instant createdAt
) {

    public static AICodeReviewResponse fromEntity(
            AICodeReview review
    ) {
        return new AICodeReviewResponse(
                review.getId(),
                review.getSubmission().getId(),
                review.getOverallFeedback(),
                review.getApproach(),
                review.getTimeComplexity(),
                review.getSpaceComplexity(),
                review.getCodeQualityScore(),
                toList(review.getStrengths()),
                toList(review.getImprovements()),
                review.getAiProvider(),
                review.getAiModel(),
                review.getCreatedAt()
        );
    }

    private static List<String> toList(String value) {

        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }

        return Arrays.stream(value.split("\\n"))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .toList();
    }
}