package com.aidsa.platform.ai;

import java.util.List;

public record AIReviewResult(
        String overallFeedback,
        String approach,
        String timeComplexity,
        String spaceComplexity,
        Integer codeQualityScore,
        List<String> strengths,
        List<String> improvements,
        String provider,
        String model
) {
}