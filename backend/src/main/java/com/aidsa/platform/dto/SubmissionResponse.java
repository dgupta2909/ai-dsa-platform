package com.aidsa.platform.dto;

import com.aidsa.platform.model.Submission;
import com.aidsa.platform.model.SubmissionStatus;

import java.time.Instant;
import java.util.List;

public class SubmissionResponse {

    private Long id;
    private Long problemId;
    private String language;
    private SubmissionStatus status;
    private Long executionTimeMs;
    private String errorMessage;
    private Instant createdAt;
    private List<TestCaseResult> testCases;

    public SubmissionResponse() {
    }

    public SubmissionResponse(
            Long id,
            Long problemId,
            String language,
            SubmissionStatus status,
            Long executionTimeMs,
            String errorMessage,
            Instant createdAt,
            List<TestCaseResult> testCases
    ) {
        this.id = id;
        this.problemId = problemId;
        this.language = language;
        this.status = status;
        this.executionTimeMs = executionTimeMs;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.testCases = testCases;
    }

    public static SubmissionResponse fromEntity(Submission submission) {

        return new SubmissionResponse(
                submission.getId(),
                submission.getProblem().getId(),
                submission.getLanguage(),
                submission.getStatus(),
                submission.getExecutionTimeMs(),
                submission.getErrorMessage(),
                submission.getCreatedAt(),
                List.of()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProblemId() {
        return problemId;
    }

    public void setProblemId(Long problemId) {
        this.problemId = problemId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public void setStatus(SubmissionStatus status) {
        this.status = status;
    }

    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(Long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<TestCaseResult> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<TestCaseResult> testCases) {
        this.testCases = testCases;
    }
}