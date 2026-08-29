package com.aidsa.platform.dto;

import com.aidsa.platform.model.Difficulty;
import com.aidsa.platform.model.Problem;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public record ProblemSummaryResponse(
        Long id,
        String title,
        String slug,
        Difficulty difficulty,
        String category,
        List<String> tags,
        Instant createdAt
) {
    public static ProblemSummaryResponse fromEntity(Problem problem) {
        List<String> tagList = problem.getTags() != null
                ? Arrays.stream(problem.getTags().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList()
                : Collections.emptyList();

        return new ProblemSummaryResponse(
                problem.getId(),
                problem.getTitle(),
                problem.getSlug(),
                problem.getDifficulty(),
                problem.getCategory(),
                tagList,
                problem.getCreatedAt()
        );
    }
}
