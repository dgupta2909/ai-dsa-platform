package com.aidsa.platform.service;

import com.aidsa.platform.dto.ProblemSummaryResponse;
import com.aidsa.platform.dto.RecommendationResponse;
import com.aidsa.platform.exception.ResourceNotFoundException;
import com.aidsa.platform.model.Difficulty;
import com.aidsa.platform.model.Problem;
import com.aidsa.platform.model.Submission;
import com.aidsa.platform.model.SubmissionStatus;
import com.aidsa.platform.model.User;
import com.aidsa.platform.model.UserProblemProgress;
import com.aidsa.platform.repository.ProblemRepository;
import com.aidsa.platform.repository.SubmissionRepository;
import com.aidsa.platform.repository.UserProblemProgressRepository;
import com.aidsa.platform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class RecommendationService {

    private static final int MAX_RECOMMENDATIONS = 10;

    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;
    private final UserProblemProgressRepository progressRepository;
    private final UserRepository userRepository;

    public RecommendationService(
            SubmissionRepository submissionRepository,
            ProblemRepository problemRepository,
            UserProblemProgressRepository progressRepository,
            UserRepository userRepository
    ) {
        this.submissionRepository = submissionRepository;
        this.problemRepository = problemRepository;
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<RecommendationResponse> getRecommendations(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId
                ));

        List<Submission> submissions =
                submissionRepository.findByUserIdOrderByCreatedAtDesc(userId);

        List<UserProblemProgress> progress =
                progressRepository.findByUser(user);

        Set<Long> solvedProblemIds = new HashSet<>();

        for (UserProblemProgress item : progress) {

            if (item.getStatus() ==
                    UserProblemProgress.Status.SOLVED &&
                    item.getProblem() != null &&
                    item.getProblem().getId() != null) {

                solvedProblemIds.add(
                        item.getProblem().getId()
                );
            }
        }

        Map<String, TopicStats> topicStats =
                buildTopicStats(submissions);

        List<Problem> problems =
                problemRepository.findAll();

        List<RecommendationCandidate> candidates =
                new ArrayList<>();

        for (Problem problem : problems) {

            if (problem.getId() == null ||
                    solvedProblemIds.contains(problem.getId())) {
                continue;
            }

            double score =
                    calculateProblemScore(
                            problem,
                            topicStats,
                            submissions,
                            progress
                    );

            String reason =
                    buildReason(
                            problem,
                            topicStats,
                            submissions
                    );

            String priority =
                    determinePriority(score);

            candidates.add(
                    new RecommendationCandidate(
                            problem,
                            score,
                            reason,
                            priority
                    )
            );
        }

        candidates.sort(
                Comparator.comparingDouble(
                        RecommendationCandidate::score
                ).reversed()
        );

        return candidates.stream()
                .limit(MAX_RECOMMENDATIONS)
                .map(candidate ->
                        new RecommendationResponse(
                                ProblemSummaryResponse.fromEntity(
                                        candidate.problem()
                                ),
                                round(candidate.score()),
                                candidate.reason(),
                                candidate.priority()
                        )
                )
                .toList();
    }

    private Map<String, TopicStats> buildTopicStats(
            List<Submission> submissions
    ) {

        Map<String, TopicStats> result =
                new HashMap<>();

        for (Submission submission : submissions) {

            Problem problem =
                    submission.getProblem();

            if (problem == null ||
                    problem.getTags() == null ||
                    problem.getTags().isBlank()) {
                continue;
            }

            String[] tags =
                    problem.getTags().split(",");

            boolean accepted =
                    submission.getStatus() ==
                            SubmissionStatus.ACCEPTED;

            for (String rawTag : tags) {

                String tag =
                        rawTag.trim().toLowerCase();

                if (tag.isBlank()) {
                    continue;
                }

                TopicStats stats =
                        result.computeIfAbsent(
                                tag,
                                key -> new TopicStats()
                        );

                stats.totalSubmissions++;

                if (accepted) {
                    stats.acceptedSubmissions++;
                } else {
                    stats.failedSubmissions++;
                }
            }
        }

        return result;
    }

    private double calculateProblemScore(
        Problem problem,
        Map<String, TopicStats> topicStats,
        List<Submission> submissions,
        List<UserProblemProgress> progress
) {
    double score = 25.0;

    List<String> tags = parseTags(problem.getTags());
    double bestWeakness = 0.0;

    for (String tag : tags) {
        TopicStats stats =
                topicStats.get(tag.toLowerCase());

        if (stats == null ||
                stats.totalSubmissions == 0) {
            continue;
        }

        double failureRate =
                stats.failedSubmissions * 100.0
                        / stats.totalSubmissions;

        double weakness =
                Math.min(failureRate, 40.0);

        if (stats.totalSubmissions >= 3) {
            weakness += 5.0;
        }

        if (stats.failedSubmissions >= 2) {
            weakness += 5.0;
        }

        bestWeakness =
                Math.max(bestWeakness, weakness);
    }

    score += Math.min(bestWeakness, 40.0);

    score += Math.min(
            difficultyAdjustment(problem, submissions),
            15.0
    );

    score += Math.min(
            attemptAdjustment(problem, progress),
            10.0
    );

    score += Math.min(
            noveltyAdjustment(problem, submissions),
            5.0
    );

    return Math.min(score, 95.0);
}

    private double difficultyAdjustment(
            Problem problem,
            List<Submission> submissions
    ) {

        if (submissions.isEmpty()) {

            return problem.getDifficulty() ==
                    Difficulty.EASY
                    ? 20.0
                    : 0.0;
        }

        long accepted =
                submissions.stream()
                        .filter(submission ->
                                submission.getStatus() ==
                                        SubmissionStatus.ACCEPTED)
                        .count();

        double successRate =
                accepted * 100.0 /
                        submissions.size();

        if (successRate >= 75.0) {

            if (problem.getDifficulty() ==
                    Difficulty.MEDIUM) {
                return 10.0;
            }

            if (problem.getDifficulty() ==
                    Difficulty.HARD) {
                return 5.0;
            }
        }

        if (successRate < 40.0 &&
                problem.getDifficulty() ==
                        Difficulty.EASY) {

            return 10.0;
        }

        return 0.0;
    }

    private double attemptAdjustment(
            Problem problem,
            List<UserProblemProgress> progress
    ) {

        for (UserProblemProgress item : progress) {

            if (item.getProblem() == null ||
                    !problem.getId().equals(
                            item.getProblem().getId())) {
                continue;
            }

            Integer attempts =
                    item.getAttempts();

            if (attempts == null) {
                return 0.0;
            }

            if (attempts >= 3) {
                return 8.0;
            }

            if (attempts == 2) {
                return 4.0;
            }

            return 0.0;
        }

        return 0.0;
    }

    private double noveltyAdjustment(
            Problem problem,
            List<Submission> submissions
    ) {

        boolean attempted =
                submissions.stream()
                        .anyMatch(submission ->
                                submission.getProblem() != null &&
                                problem.getId().equals(
                                        submission.getProblem().getId()
                                ));

        return attempted ? 0.0 : 5.0;
    }

  private String buildReason(
        Problem problem,
        Map<String, TopicStats> topicStats,
        List<Submission> submissions
) {
    String weakestTag = null;
    double highestWeakness = -1.0;

    for (String tag : parseTags(problem.getTags())) {
        TopicStats stats = topicStats.get(tag.toLowerCase());

        if (stats == null || stats.totalSubmissions == 0) {
            continue;
        }

        double failureRate =
                stats.failedSubmissions * 100.0
                        / stats.totalSubmissions;

        if (failureRate > highestWeakness) {
            highestWeakness = failureRate;
            weakestTag = tag;
        }
    }

    if (weakestTag != null && highestWeakness >= 50.0) {
        return "Recommended to strengthen your "
                + weakestTag
                + " skills based on your recent performance.";
    }

    long problemAttempts = submissions.stream()
            .filter(submission ->
                    submission.getProblem() != null
                            && problem.getId().equals(
                            submission.getProblem().getId()
                    )
            )
            .count();

    if (problemAttempts >= 3) {
        return "You've attempted this problem multiple times. "
                + "More practice may help reinforce the concept.";
    }

    if (problemAttempts > 0) {
        return "You've attempted this problem before. "
                + "Practicing it again can help strengthen your understanding.";
    }

    if (submissions.isEmpty()) {
        return "A beginner-friendly problem to start building your DSA skills.";
    }

    if (highestWeakness >= 25.0) {
        return "Practice this problem to improve a topic "
                + "where you have had some difficulty.";
    }

    return "Recommended based on your current DSA progress.";
}

    private String determinePriority(double score) {

        if (score >= 75.0) {
            return "HIGH";
        }

        if (score >= 50.0) {
            return "MEDIUM";
        }

        return "LOW";
    }

    private List<String> parseTags(String tags) {

        if (tags == null ||
                tags.isBlank()) {
            return List.of();
        }

        List<String> result =
                new ArrayList<>();

        for (String tag : tags.split(",")) {

            String cleaned =
                    tag.trim();

            if (!cleaned.isBlank()) {
                result.add(cleaned);
            }
        }

        return result;
    }

    private double round(double value) {

        return Math.round(value * 100.0)
                / 100.0;
    }

    private record RecommendationCandidate(
            Problem problem,
            double score,
            String reason,
            String priority
    ) {
    }

    private static class TopicStats {

        private long totalSubmissions;
        private long acceptedSubmissions;
        private long failedSubmissions;
    }
}