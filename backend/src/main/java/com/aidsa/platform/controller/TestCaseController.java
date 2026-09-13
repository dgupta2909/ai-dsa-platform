package com.aidsa.platform.controller;

import com.aidsa.platform.dto.TestCaseRequest;
import com.aidsa.platform.dto.TestCaseResponse;
import com.aidsa.platform.service.TestCaseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/problems/{problemId}/test-cases")
public class TestCaseController {

    private final TestCaseService testCaseService;

    public TestCaseController(TestCaseService testCaseService) {
        this.testCaseService = testCaseService;
    }

    @GetMapping
    public ResponseEntity<List<TestCaseResponse>> getTestCases(
            @PathVariable Long problemId) {

        return ResponseEntity.ok(
                testCaseService.getTestCases(problemId));
    }

    @PostMapping
    public ResponseEntity<TestCaseResponse> createTestCase(
            @PathVariable Long problemId,
            @Valid @RequestBody TestCaseRequest request) {

        TestCaseResponse response = testCaseService.createTestCase(problemId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}