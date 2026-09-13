package com.aidsa.platform.dto;

public record TestCaseResult(
        int testCaseNumber,
        Long testCaseId,
        boolean hidden,
        String status,
        Long executionTimeMs,
        String inputData,
        String expectedOutput,
        String actualOutput,
        String errorMessage
) {
}