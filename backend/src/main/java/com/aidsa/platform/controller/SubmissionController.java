package com.aidsa.platform.controller;

import com.aidsa.platform.dto.SubmissionAnalyticsResponse;
import com.aidsa.platform.dto.SubmissionRequest;
import com.aidsa.platform.dto.SubmissionResponse;
import com.aidsa.platform.security.UserPrincipal;
import com.aidsa.platform.service.SubmissionAnalyticsService;
import com.aidsa.platform.service.SubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;
    private final SubmissionAnalyticsService submissionAnalyticsService;

    public SubmissionController(
            SubmissionService submissionService,
            SubmissionAnalyticsService submissionAnalyticsService
    ) {
        this.submissionService = submissionService;
        this.submissionAnalyticsService =
                submissionAnalyticsService;
    }

    @PostMapping
    public ResponseEntity<SubmissionResponse> submit(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody SubmissionRequest request
    ) {

        SubmissionResponse response =
                submissionService.submit(
                        principal.id(),
                        request
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<SubmissionResponse>>
    getSubmissionHistory(
            @AuthenticationPrincipal UserPrincipal principal
    ) {

        return ResponseEntity.ok(
                submissionService.getSubmissionHistory(
                        principal.id()
                )
        );
    }

    @GetMapping("/analytics")
    public ResponseEntity<SubmissionAnalyticsResponse>
    getSubmissionAnalytics(
            @AuthenticationPrincipal UserPrincipal principal
    ) {

        return ResponseEntity.ok(
                submissionAnalyticsService.getAnalytics(
                        principal.id()
                )
        );
    }
}