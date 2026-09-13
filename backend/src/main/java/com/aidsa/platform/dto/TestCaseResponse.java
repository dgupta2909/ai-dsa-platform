package com.aidsa.platform.dto;

import com.aidsa.platform.model.TestCase;

public record TestCaseResponse(
        Long id,
        Long problemId,
        String inputData,
        String expectedOutput,
        boolean hidden
) {

    public static TestCaseResponse fromEntity(TestCase testCase) {
        return new TestCaseResponse(
                testCase.getId(),
                testCase.getProblem().getId(),
                testCase.getInputData(),
                testCase.getExpectedOutput(),
                testCase.isHidden()
        );
    }
}