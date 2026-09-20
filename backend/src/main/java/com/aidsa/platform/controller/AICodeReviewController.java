package com.aidsa.platform.controller;

import com.aidsa.platform.dto.AICodeReviewResponse;
import com.aidsa.platform.model.Submission;
import com.aidsa.platform.security.UserPrincipal;
import com.aidsa.platform.service.AICodeReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai-reviews")
public class AICodeReviewController {

    private final AICodeReviewService aiCodeReviewService;

    public AICodeReviewController(
            AICodeReviewService aiCodeReviewService
    ) {
        this.aiCodeReviewService = aiCodeReviewService;
    }

    @PostMapping("/{submissionId}")
    public ResponseEntity<AICodeReviewResponse> generateReview(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long submissionId
    ) {

        Submission submission =
                aiCodeReviewService.getSubmission(submissionId);

        verifyOwnership(
                principal,
                submission
        );

        return ResponseEntity.ok(
                aiCodeReviewService.generateReview(
                        submissionId
                )
        );
    }

    @PostMapping("/{submissionId}/regenerate")
    public ResponseEntity<AICodeReviewResponse> regenerateReview(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long submissionId
    ) {

        Submission submission =
                aiCodeReviewService.getSubmission(submissionId);

        verifyOwnership(
                principal,
                submission
        );

        return ResponseEntity.ok(
                aiCodeReviewService.regenerateReview(
                        submissionId
                )
        );
    }

    @GetMapping("/{submissionId}")
    public ResponseEntity<AICodeReviewResponse> getReview(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long submissionId
    ) {

        Submission submission =
                aiCodeReviewService.getSubmission(submissionId);

        verifyOwnership(
                principal,
                submission
        );

        return ResponseEntity.ok(
                aiCodeReviewService.getReview(
                        submissionId
                )
        );
    }

    private void verifyOwnership(
            UserPrincipal principal,
            Submission submission
    ) {

        if (!submission.getUser().getId().equals(principal.id())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You do not have access to this submission"
            );
        }
    }
}