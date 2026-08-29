package com.aidsa.platform.dto;

import com.aidsa.platform.model.Difficulty;
import com.aidsa.platform.model.Problem;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public record ProblemDetailResponse(
        Long id,
        Integer problemNumber,
        String title,
        String slug,
        String description,
        String constraints,
        Difficulty difficulty,
        String category,
        List<String> tags,
        Instant createdAt,
        Instant updatedAt
) {
    public static ProblemDetailResponse fromEntity(Problem problem) {
        List<String> tagList = problem.getTags() != null
                ? Arrays.stream(problem.getTags().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList()
                : Collections.emptyList();

        return new ProblemDetailResponse(
                problem.getId(),
                problem.getProblemNumber(),
                problem.getTitle(),
                problem.getSlug(),
                problem.getDescription(),
                problem.getConstraints(),
                problem.getDifficulty(),
                problem.getCategory(),
                tagList,
                problem.getCreatedAt(),
                problem.getUpdatedAt()
        );
    }
}
