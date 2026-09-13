package com.aidsa.platform.service;

import com.aidsa.platform.dto.CodeRunRequest;
import com.aidsa.platform.dto.SubmissionRequest;
import com.aidsa.platform.dto.SubmissionResponse;
import com.aidsa.platform.dto.TestCaseResult;
import com.aidsa.platform.exception.ResourceNotFoundException;
import com.aidsa.platform.model.Problem;
import com.aidsa.platform.model.Submission;
import com.aidsa.platform.model.SubmissionStatus;
import com.aidsa.platform.model.TestCase;
import com.aidsa.platform.model.User;
import com.aidsa.platform.repository.ProblemRepository;
import com.aidsa.platform.repository.SubmissionRepository;
import com.aidsa.platform.repository.TestCaseRepository;
import com.aidsa.platform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;
    private final CodeExecutionService codeExecutionService;
    private final ProblemProgressService problemProgressService;

    public SubmissionService(
            SubmissionRepository submissionRepository,
            UserRepository userRepository,
            ProblemRepository problemRepository,
            TestCaseRepository testCaseRepository,
            CodeExecutionService codeExecutionService,
            ProblemProgressService problemProgressService
    ) {
        this.submissionRepository = submissionRepository;
        this.userRepository = userRepository;
        this.problemRepository = problemRepository;
        this.testCaseRepository = testCaseRepository;
        this.codeExecutionService = codeExecutionService;
        this.problemProgressService = problemProgressService;
    }
@Transactional(readOnly = true)
public List<SubmissionResponse> getSubmissionHistory(Long userId) {

    List<Submission> submissions =
            submissionRepository.findByUserIdOrderByCreatedAtDesc(userId);

    return submissions.stream()
            .map(SubmissionResponse::fromEntity)
            .toList();
}

    @Transactional
    public SubmissionResponse submit(Long userId, SubmissionRequest request) {

        validateRequest(request);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId
                ));

        Problem problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Problem not found with id: " + request.getProblemId()
                ));

        List<TestCase> testCases =
                testCaseRepository.findByProblemId(problem.getId());

        if (testCases.isEmpty()) {
            throw new IllegalStateException(
                    "No test cases configured for this problem."
            );
        }

        /*
         * Every submission counts as an attempt.
         */
        problemProgressService.markAttempted(
                userId,
                problem.getId()
        );

        SubmissionStatus finalStatus = SubmissionStatus.ACCEPTED;
        String errorMessage = "";
        long totalExecutionTimeMs = 0;

        List<TestCaseResult> testCaseResults = new ArrayList<>();

        CodeRunRequest codeRunRequest = new CodeRunRequest(
                request.getLanguage(),
                request.getCode()
        );

        int testCaseNumber = 1;

        for (TestCase testCase : testCases) {

            long startTime = System.nanoTime();

            CodeExecutionResult result =
                    codeExecutionService.runCodeForSubmission(
                            codeRunRequest,
                            testCase.getInputData()
                    );

            long executionTimeMs =
                    (System.nanoTime() - startTime) / 1_000_000;

            totalExecutionTimeMs += executionTimeMs;

            String testCaseStatus;
            String testCaseError = null;
            String actualOutput = result.getOutput();

            /*
             * TIME LIMIT EXCEEDED
             */
            if (result.isTimedOut()) {

                finalStatus = SubmissionStatus.TIME_LIMIT_EXCEEDED;
                errorMessage = result.getError();

                testCaseStatus = "TIME_LIMIT_EXCEEDED";
                testCaseError = result.getError();

                testCaseResults.add(
                        createTestCaseResult(
                                testCaseNumber,
                                testCase,
                                testCaseStatus,
                                executionTimeMs,
                                actualOutput,
                                testCaseError
                        )
                );

                break;
            }

            /*
             * COMPILATION ERROR
             */
            if (result.isCompilationError()) {

                finalStatus = SubmissionStatus.COMPILATION_ERROR;
                errorMessage = result.getError();

                testCaseStatus = "COMPILATION_ERROR";
                testCaseError = result.getError();

                testCaseResults.add(
                        createTestCaseResult(
                                testCaseNumber,
                                testCase,
                                testCaseStatus,
                                executionTimeMs,
                                actualOutput,
                                testCaseError
                        )
                );

                break;
            }

            /*
             * RUNTIME ERROR
             */
            if (!result.isSuccess()) {

                finalStatus = SubmissionStatus.RUNTIME_ERROR;
                errorMessage = result.getError();

                testCaseStatus = "RUNTIME_ERROR";
                testCaseError = result.getError();

                testCaseResults.add(
                        createTestCaseResult(
                                testCaseNumber,
                                testCase,
                                testCaseStatus,
                                executionTimeMs,
                                actualOutput,
                                testCaseError
                        )
                );

                break;
            }

            /*
             * WRONG ANSWER
             */
            if (!outputsMatch(
                    result.getOutput(),
                    testCase.getExpectedOutput()
            )) {

                finalStatus = SubmissionStatus.WRONG_ANSWER;
                errorMessage = "Output did not match the expected result.";

                testCaseStatus = "FAILED";
                testCaseError = "Output did not match the expected result.";

                testCaseResults.add(
                        createTestCaseResult(
                                testCaseNumber,
                                testCase,
                                testCaseStatus,
                                executionTimeMs,
                                actualOutput,
                                testCaseError
                        )
                );

                break;
            }

            /*
             * PASSED
             */
            testCaseStatus = "PASSED";

            testCaseResults.add(
                    createTestCaseResult(
                            testCaseNumber,
                            testCase,
                            testCaseStatus,
                            executionTimeMs,
                            actualOutput,
                            null
                    )
            );

            testCaseNumber++;
        }

        /*
         * Mark problem solved only when every test case passed.
         */
        if (finalStatus == SubmissionStatus.ACCEPTED) {

            problemProgressService.markSolved(
                    userId,
                    problem.getId()
            );
        }

        Submission submission = new Submission(
                user,
                problem,
                request.getLanguage(),
                request.getCode(),
                finalStatus,
                totalExecutionTimeMs,
                errorMessage
        );

        Submission savedSubmission =
                submissionRepository.save(submission);

        return new SubmissionResponse(
                savedSubmission.getId(),
                savedSubmission.getProblem().getId(),
                savedSubmission.getLanguage(),
                savedSubmission.getStatus(),
                savedSubmission.getExecutionTimeMs(),
                savedSubmission.getErrorMessage(),
                savedSubmission.getCreatedAt(),
                testCaseResults
        );
    }

    /*
     * Creates the API representation of a test case.
     *
     * Hidden test cases never expose:
     * - input
     * - expected output
     * - actual output
     *
     * This prevents hidden judge data from leaking to users.
     */
    private TestCaseResult createTestCaseResult(
            int testCaseNumber,
            TestCase testCase,
            String status,
            Long executionTimeMs,
            String actualOutput,
            String errorMessage
    ) {

        String inputData = null;
        String expectedOutput = null;
        String safeActualOutput = null;

        if (!testCase.isHidden()) {
            inputData = testCase.getInputData();
            expectedOutput = testCase.getExpectedOutput();
            safeActualOutput = actualOutput;
        }

        return new TestCaseResult(
                testCaseNumber,
                testCase.getId(),
                testCase.isHidden(),
                status,
                executionTimeMs,
                inputData,
                expectedOutput,
                safeActualOutput,
                errorMessage
        );
    }

    private void validateRequest(SubmissionRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Submission request cannot be null."
            );
        }

        if (request.getProblemId() == null) {
            throw new IllegalArgumentException(
                    "Problem ID is required."
            );
        }

        if (request.getLanguage() == null ||
                request.getLanguage().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Programming language is required."
            );
        }

        if (request.getCode() == null ||
                request.getCode().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Code cannot be empty."
            );
        }
    }

    /*
     * Compare outputs while ignoring:
     * - leading/trailing whitespace
     * - Windows vs Linux line endings
     * - extra whitespace between tokens
     */
    private boolean outputsMatch(
            String actual,
            String expected
    ) {

        String normalizedActual =
                normalizeOutput(actual);

        String normalizedExpected =
                normalizeOutput(expected);

        return normalizedActual.equals(normalizedExpected);
    }

    private String normalizeOutput(String output) {

        if (output == null) {
            return "";
        }

        return output
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .trim()
                .replaceAll("\\s+", " ");
    }
}