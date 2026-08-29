package com.aidsa.platform.dto;

import com.aidsa.platform.model.UserProblemProgress;

import java.time.OffsetDateTime;

public record ProblemProgressResponse(
        Long problemId,
        String status,
        Integer attempts,
        OffsetDateTime solvedAt,
        OffsetDateTime lastAttemptedAt) {
    public static ProblemProgressResponse fromEntity(UserProblemProgress progress) {
        return new ProblemProgressResponse(
                progress.getProblem().getId(),
                progress.getStatus().name(),
                progress.getAttempts(),
                progress.getSolvedAt(),
                progress.getLastAttemptedAt());
    }
}