package com.aidsa.platform.dto;

import java.util.List;
import java.util.Map;

public record SubmissionAnalyticsResponse(

        long totalSubmissions,

        long acceptedSubmissions,

        long failedSubmissions,

        double successRate,

        Double averageExecutionTimeMs,

        Long fastestExecutionTimeMs,

        Long slowestExecutionTimeMs,

        Map<String, DifficultyStats> difficultyStats,

        Map<String, Long> statusBreakdown,

        List<DailySubmissionStats> dailyActivity

) {

    public record DifficultyStats(
            long submissions,
            long accepted,
            long failed,
            double successRate
    ) {}

    public record DailySubmissionStats(
            String date,
            long submissions,
            long accepted
    ) {}
}