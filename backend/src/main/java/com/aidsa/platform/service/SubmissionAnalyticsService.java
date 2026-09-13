package com.aidsa.platform.service;

import com.aidsa.platform.dto.SubmissionAnalyticsResponse;
import com.aidsa.platform.model.Difficulty;
import com.aidsa.platform.model.Submission;
import com.aidsa.platform.model.SubmissionStatus;
import com.aidsa.platform.repository.SubmissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SubmissionAnalyticsService {

    private static final ZoneId INDIA_ZONE =
            ZoneId.of("Asia/Kolkata");

    private final SubmissionRepository submissionRepository;

    public SubmissionAnalyticsService(
            SubmissionRepository submissionRepository
    ) {
        this.submissionRepository = submissionRepository;
    }

    @Transactional(readOnly = true)
    public SubmissionAnalyticsResponse getAnalytics(Long userId) {

        List<Submission> submissions =
                submissionRepository.findByUserIdOrderByCreatedAtDesc(userId);

        long totalSubmissions = submissions.size();

        long acceptedSubmissions = submissions.stream()
                .filter(submission ->
                        submission.getStatus() == SubmissionStatus.ACCEPTED)
                .count();

        long failedSubmissions =
                totalSubmissions - acceptedSubmissions;

        double successRate =
                totalSubmissions == 0
                        ? 0.0
                        : roundPercentage(
                                (acceptedSubmissions * 100.0)
                                        / totalSubmissions
                        );

        List<Long> executionTimes = submissions.stream()
                .map(Submission::getExecutionTimeMs)
                .filter(time -> time != null && time >= 0)
                .toList();

        Double averageExecutionTimeMs = executionTimes.isEmpty()
                ? null
                : roundDouble(
                        executionTimes.stream()
                                .mapToLong(Long::longValue)
                                .average()
                                .orElse(0.0)
                );

        Long fastestExecutionTimeMs = executionTimes.isEmpty()
                ? null
                : executionTimes.stream()
                        .min(Long::compareTo)
                        .orElse(null);

        Long slowestExecutionTimeMs = executionTimes.isEmpty()
                ? null
                : executionTimes.stream()
                        .max(Long::compareTo)
                        .orElse(null);

        Map<String, Long> statusBreakdown =
                buildStatusBreakdown(submissions);

        Map<String, SubmissionAnalyticsResponse.DifficultyStats>
                difficultyStats =
                buildDifficultyStats(submissions);

        List<SubmissionAnalyticsResponse.DailySubmissionStats>
                dailyActivity =
                buildDailyActivity(submissions);

        return new SubmissionAnalyticsResponse(
                totalSubmissions,
                acceptedSubmissions,
                failedSubmissions,
                successRate,
                averageExecutionTimeMs,
                fastestExecutionTimeMs,
                slowestExecutionTimeMs,
                difficultyStats,
                statusBreakdown,
                dailyActivity
        );
    }

    private Map<String, Long> buildStatusBreakdown(
            List<Submission> submissions
    ) {

        Map<String, Long> result = new LinkedHashMap<>();

        for (SubmissionStatus status : SubmissionStatus.values()) {
            result.put(status.name(), 0L);
        }

        for (Submission submission : submissions) {

            SubmissionStatus status =
                    submission.getStatus();

            if (status != null) {
                result.put(
                        status.name(),
                        result.getOrDefault(status.name(), 0L) + 1
                );
            }
        }

        return result;
    }

    private Map<String, SubmissionAnalyticsResponse.DifficultyStats>
    buildDifficultyStats(List<Submission> submissions) {

        Map<Difficulty, Long> totalByDifficulty =
                new EnumMap<>(Difficulty.class);

        Map<Difficulty, Long> acceptedByDifficulty =
                new EnumMap<>(Difficulty.class);

        for (Difficulty difficulty : Difficulty.values()) {
            totalByDifficulty.put(difficulty, 0L);
            acceptedByDifficulty.put(difficulty, 0L);
        }

        for (Submission submission : submissions) {

            if (submission.getProblem() == null ||
                    submission.getProblem().getDifficulty() == null) {
                continue;
            }

            Difficulty difficulty =
                    submission.getProblem().getDifficulty();

            totalByDifficulty.put(
                    difficulty,
                    totalByDifficulty.getOrDefault(difficulty, 0L) + 1
            );

            if (submission.getStatus() ==
                    SubmissionStatus.ACCEPTED) {

                acceptedByDifficulty.put(
                        difficulty,
                        acceptedByDifficulty.getOrDefault(
                                difficulty,
                                0L
                        ) + 1
                );
            }
        }

        Map<String, SubmissionAnalyticsResponse.DifficultyStats>
                result = new LinkedHashMap<>();

        for (Difficulty difficulty : Difficulty.values()) {

            long total =
                    totalByDifficulty.getOrDefault(difficulty, 0L);

            long accepted =
                    acceptedByDifficulty.getOrDefault(difficulty, 0L);

            long failed = total - accepted;

            double rate =
                    total == 0
                            ? 0.0
                            : roundPercentage(
                                    (accepted * 100.0) / total
                            );

            result.put(
                    difficulty.name(),
                    new SubmissionAnalyticsResponse.DifficultyStats(
                            total,
                            accepted,
                            failed,
                            rate
                    )
            );
        }

        return result;
    }

    private List<SubmissionAnalyticsResponse.DailySubmissionStats>
    buildDailyActivity(List<Submission> submissions) {

        LocalDate today =
                LocalDate.now(INDIA_ZONE);

        Map<LocalDate, Long> submissionsByDate =
                new LinkedHashMap<>();

        Map<LocalDate, Long> acceptedByDate =
                new LinkedHashMap<>();

        for (int i = 6; i >= 0; i--) {

            LocalDate date =
                    today.minusDays(i);

            submissionsByDate.put(date, 0L);
            acceptedByDate.put(date, 0L);
        }

        for (Submission submission : submissions) {

            Instant createdAt =
                    submission.getCreatedAt();

            if (createdAt == null) {
                continue;
            }

            LocalDate date =
                    createdAt
                            .atZone(INDIA_ZONE)
                            .toLocalDate();

            if (!submissionsByDate.containsKey(date)) {
                continue;
            }

            submissionsByDate.put(
                    date,
                    submissionsByDate.get(date) + 1
            );

            if (submission.getStatus() ==
                    SubmissionStatus.ACCEPTED) {

                acceptedByDate.put(
                        date,
                        acceptedByDate.get(date) + 1
                );
            }
        }

        List<SubmissionAnalyticsResponse.DailySubmissionStats>
                result = new ArrayList<>();

        for (LocalDate date : submissionsByDate.keySet()) {

            result.add(
                    new SubmissionAnalyticsResponse.DailySubmissionStats(
                            date.toString(),
                            submissionsByDate.get(date),
                            acceptedByDate.get(date)
                    )
            );
        }

        return result;
    }

    private double roundPercentage(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private double roundDouble(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}