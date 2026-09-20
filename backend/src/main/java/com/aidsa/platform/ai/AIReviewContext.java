package com.aidsa.platform.ai;

import com.aidsa.platform.dto.TestCaseResult;
import com.aidsa.platform.model.SubmissionStatus;

public record AIReviewContext(
        SubmissionStatus submissionStatus,
        String submissionErrorMessage,
        Long executionTimeMs,
        TestCaseResult failedTestCase
) {
}