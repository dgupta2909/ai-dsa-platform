package com.aidsa.platform.service;

import com.aidsa.platform.dto.TestCaseRequest;
import com.aidsa.platform.dto.TestCaseResponse;
import com.aidsa.platform.exception.ResourceNotFoundException;
import com.aidsa.platform.model.Problem;
import com.aidsa.platform.model.TestCase;
import com.aidsa.platform.repository.ProblemRepository;
import com.aidsa.platform.repository.TestCaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TestCaseService {

    private final TestCaseRepository testCaseRepository;
    private final ProblemRepository problemRepository;

    public TestCaseService(
            TestCaseRepository testCaseRepository,
            ProblemRepository problemRepository) {

        this.testCaseRepository = testCaseRepository;
        this.problemRepository = problemRepository;
    }

    @Transactional(readOnly = true)
    public List<TestCaseResponse> getTestCases(Long problemId) {

        getProblem(problemId);

        return testCaseRepository.findByProblemId(problemId)
                .stream()
                .map(this::toSafeResponse)
                .toList();
    }

    @Transactional
    public TestCaseResponse createTestCase(
            Long problemId,
            TestCaseRequest request) {

        Problem problem = getProblem(problemId);

        TestCase testCase = new TestCase(
                problem,
                request.inputData(),
                request.expectedOutput(),
                request.hidden());

        return TestCaseResponse.fromEntity(
                testCaseRepository.save(testCase));
    }

    private TestCaseResponse toSafeResponse(TestCase testCase) {

        if (testCase.isHidden()) {
            return new TestCaseResponse(
                    testCase.getId(),
                    testCase.getProblem().getId(),
                    null,
                    null,
                    true
            );
        }

        return TestCaseResponse.fromEntity(testCase);
    }

    private Problem getProblem(Long problemId) {

        return problemRepository.findById(problemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Problem not found with id: " + problemId));
    }
}