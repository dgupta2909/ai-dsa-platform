package com.aidsa.platform.service;

import com.aidsa.platform.dto.RecommendationResponse;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private UserProblemProgressRepository progressRepository;

    @Mock
    private UserRepository userRepository;

    private RecommendationService service;

    private User user;

    @BeforeEach
    void setUp() {
        service = new RecommendationService(
                submissionRepository,
                problemRepository,
                progressRepository,
                userRepository
        );

        user = new User(
                "Test User",
                "test@example.com",
                "password"
        );
        user.setId(1L);
    }

    @Test
    void noSubmissionsReturnsBeginnerFriendlyRecommendations() {

        Problem easyProblem = problem(
                1L,
                1,
                "Two Sum Practice",
                Difficulty.EASY,
                "Arrays",
                "array,hashmap"
        );

        Problem mediumProblem = problem(
                2L,
                2,
                "Binary Search Practice",
                Difficulty.MEDIUM,
                "Searching",
                "binary-search,array"
        );

        when(userRepository.findById(1L))
                .thenReturn(java.util.Optional.of(user));

        when(submissionRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of());

        when(progressRepository.findByUser(user))
                .thenReturn(List.of());

        when(problemRepository.findAll())
                .thenReturn(List.of(
                        easyProblem,
                        mediumProblem
                ));

        List<RecommendationResponse> recommendations =
                service.getRecommendations(1L);

        assertEquals(2, recommendations.size());

        RecommendationResponse first =
                recommendations.get(0);

        assertEquals(
                "A beginner-friendly problem to start building your DSA skills.",
                first.reason()
        );

        assertTrue(first.score() > 0);
    }

    @Test
    void weakTopicProblemGetsHigherScore() {

        Problem weakTopicProblem = problem(
                1L,
                1,
                "Array Challenge",
                Difficulty.EASY,
                "Arrays",
                "array"
        );

        Problem unrelatedProblem = problem(
                2L,
                2,
                "Graph Challenge",
                Difficulty.EASY,
                "Graphs",
                "graph"
        );

        Submission failed1 = submission(
                weakTopicProblem,
                SubmissionStatus.WRONG_ANSWER
        );

        Submission failed2 = submission(
                weakTopicProblem,
                SubmissionStatus.RUNTIME_ERROR
        );

        Submission accepted = submission(
                unrelatedProblem,
                SubmissionStatus.ACCEPTED
        );

        when(userRepository.findById(1L))
                .thenReturn(java.util.Optional.of(user));

        when(submissionRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(
                        failed1,
                        failed2,
                        accepted
                ));

        when(progressRepository.findByUser(user))
                .thenReturn(List.of());

        when(problemRepository.findAll())
                .thenReturn(List.of(
                        weakTopicProblem,
                        unrelatedProblem
                ));

        List<RecommendationResponse> recommendations =
                service.getRecommendations(1L);

        assertEquals(
                "Array Challenge",
                recommendations.get(0).problem().title()
        );

        assertTrue(
                recommendations.get(0).score()
                        > recommendations.get(1).score()
        );

        assertTrue(
                recommendations.get(0).reason()
                        .toLowerCase()
                        .contains("array")
        );
    }

    @Test
    void solvedProblemsAreNotRecommended() {

        Problem solvedProblem = problem(
                1L,
                1,
                "Solved Problem",
                Difficulty.EASY,
                "Arrays",
                "array"
        );

        Problem unsolvedProblem = problem(
                2L,
                2,
                "Unsolved Problem",
                Difficulty.EASY,
                "Arrays",
                "array"
        );

        UserProblemProgress solvedProgress =
                progress(
                        solvedProblem,
                        UserProblemProgress.Status.SOLVED,
                        1
                );

        when(userRepository.findById(1L))
                .thenReturn(java.util.Optional.of(user));

        when(submissionRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of());

        when(progressRepository.findByUser(user))
                .thenReturn(List.of(solvedProgress));

        when(problemRepository.findAll())
                .thenReturn(List.of(
                        solvedProblem,
                        unsolvedProblem
                ));

        List<RecommendationResponse> recommendations =
                service.getRecommendations(1L);

        assertEquals(1, recommendations.size());

        assertEquals(
                "Unsolved Problem",
                recommendations.get(0).problem().title()
        );
    }

    @Test
    void repeatedAttemptsIncreaseRecommendationScore() {

        Problem problem = problem(
                1L,
                1,
                "Difficult Array Problem",
                Difficulty.MEDIUM,
                "Arrays",
                "array"
        );

        UserProblemProgress progress =
                progress(
                        problem,
                        UserProblemProgress.Status.ATTEMPTED,
                        3
                );

        when(userRepository.findById(1L))
                .thenReturn(java.util.Optional.of(user));

        when(submissionRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of());

        when(progressRepository.findByUser(user))
                .thenReturn(List.of(progress));

        when(problemRepository.findAll())
                .thenReturn(List.of(problem));

        List<RecommendationResponse> recommendations =
                service.getRecommendations(1L);

        assertEquals(1, recommendations.size());

        /*
         * Base score = 25
         * No submission history = EASY adjustment does not apply to MEDIUM
         * 3 attempts = +8
         * Novel problem = +5
         *
         * Expected score = 38
         */
        assertEquals(
                38.0,
                recommendations.get(0).score()
        );
    }

    @Test
    void strongPerformanceCanRecommendMediumBeforeHard() {

        Problem mediumProblem = problem(
                1L,
                1,
                "Medium Array Problem",
                Difficulty.MEDIUM,
                "Arrays",
                "array"
        );

        Problem hardProblem = problem(
                2L,
                2,
                "Hard Graph Problem",
                Difficulty.HARD,
                "Graphs",
                "graph"
        );

        Submission accepted1 =
                submission(
                        mediumProblem,
                        SubmissionStatus.ACCEPTED
                );

        Submission accepted2 =
                submission(
                        hardProblem,
                        SubmissionStatus.ACCEPTED
                );

        Submission accepted3 =
                submission(
                        mediumProblem,
                        SubmissionStatus.ACCEPTED
                );

        Submission accepted4 =
                submission(
                        hardProblem,
                        SubmissionStatus.ACCEPTED
                );

        when(userRepository.findById(1L))
                .thenReturn(java.util.Optional.of(user));

        when(submissionRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(
                        accepted1,
                        accepted2,
                        accepted3,
                        accepted4
                ));

        when(progressRepository.findByUser(user))
                .thenReturn(List.of());

        when(problemRepository.findAll())
                .thenReturn(List.of(
                        mediumProblem,
                        hardProblem
                ));

        List<RecommendationResponse> recommendations =
                service.getRecommendations(1L);

        assertEquals(2, recommendations.size());

        assertTrue(
                recommendations.stream()
                        .allMatch(item -> item.score() >= 30.0)
        );
    }

    @Test
    void multipleAttemptsProduceSpecificRecommendationReason() {

        Problem targetProblem = problem(
                1L,
                1,
                "Repeated Array Problem",
                Difficulty.MEDIUM,
                "Arrays",
                "array"
        );

        Submission attempt1 =
                submission(
                        targetProblem,
                        SubmissionStatus.WRONG_ANSWER
                );

        Submission attempt2 =
                submission(
                        targetProblem,
                        SubmissionStatus.RUNTIME_ERROR
                );

        Submission attempt3 =
                submission(
                        targetProblem,
                        SubmissionStatus.WRONG_ANSWER
                );

        when(userRepository.findById(1L))
                .thenReturn(java.util.Optional.of(user));

        when(submissionRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(
                        attempt1,
                        attempt2,
                        attempt3
                ));

        when(progressRepository.findByUser(user))
                .thenReturn(List.of());

        when(problemRepository.findAll())
                .thenReturn(List.of(targetProblem));

        List<RecommendationResponse> recommendations =
                service.getRecommendations(1L);

        assertEquals(1, recommendations.size());

        assertEquals(
                "Recommended to strengthen your array skills based on your recent performance.",
                recommendations.get(0).reason()
        );
    }

    @Test
    void previouslyAttemptedProblemGetsPracticeReason() {

        Problem targetProblem = problem(
                1L,
                1,
                "Array Practice",
                Difficulty.MEDIUM,
                "Arrays",
                "array"
        );

        Submission attempt =
                submission(
                        targetProblem,
                        SubmissionStatus.ACCEPTED
                );

        when(userRepository.findById(1L))
                .thenReturn(java.util.Optional.of(user));

        when(submissionRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(attempt));

        when(progressRepository.findByUser(user))
                .thenReturn(List.of());

        when(problemRepository.findAll())
                .thenReturn(List.of(targetProblem));

        List<RecommendationResponse> recommendations =
                service.getRecommendations(1L);

        assertEquals(1, recommendations.size());

        assertEquals(
                "You've attempted this problem before. Practicing it again can help strengthen your understanding.",
                recommendations.get(0).reason()
        );
    }

    private Problem problem(
            Long id,
            Integer number,
            String title,
            Difficulty difficulty,
            String category,
            String tags
    ) {
        Problem problem = new Problem(
                number,
                title,
                title.toLowerCase()
                        .replace(" ", "-"),
                "Test problem description",
                "Test constraints",
                difficulty,
                category,
                tags
        );

        problem.setId(id);

        return problem;
    }

    private Submission submission(
            Problem problem,
            SubmissionStatus status
    ) {
        return new Submission(
                user,
                problem,
                "java",
                "test code",
                status,
                100L,
                null
        );
    }

    private UserProblemProgress progress(
            Problem problem,
            UserProblemProgress.Status status,
            int attempts
    ) {
        UserProblemProgress progress =
                new UserProblemProgress();

        progress.setUser(user);
        progress.setProblem(problem);
        progress.setStatus(status);
        progress.setAttempts(attempts);

        return progress;
    }
}