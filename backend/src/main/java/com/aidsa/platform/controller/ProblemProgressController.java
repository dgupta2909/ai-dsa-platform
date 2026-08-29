package com.aidsa.platform.controller;

import com.aidsa.platform.dto.ProblemProgressResponse;
import com.aidsa.platform.security.UserPrincipal;
import com.aidsa.platform.service.ProblemProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
public class ProblemProgressController {

    private final ProblemProgressService progressService;

    public ProblemProgressController(
            ProblemProgressService progressService) {
        this.progressService = progressService;
    }

    @PostMapping("/{problemId}/attempt")
    public ResponseEntity<ProblemProgressResponse> markAttempted(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long problemId) {

        ProblemProgressResponse response = progressService.markAttempted(
                principal.id(),
                problemId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{problemId}/solve")
    public ResponseEntity<ProblemProgressResponse> markSolved(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long problemId) {

        ProblemProgressResponse response = progressService.markSolved(
                principal.id(),
                problemId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{problemId}")
    public ResponseEntity<ProblemProgressResponse> getProgress(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long problemId) {

        ProblemProgressResponse response = progressService.getProgress(
                principal.id(),
                problemId);

        if (response == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ProblemProgressResponse>> getUserProgress(
            @AuthenticationPrincipal UserPrincipal principal) {

        return ResponseEntity.ok(
                progressService.getUserProgress(principal.id()));
    }

    @GetMapping("/solved-count")
    public ResponseEntity<Long> getSolvedCount(
            @AuthenticationPrincipal UserPrincipal principal) {

        return ResponseEntity.ok(
                progressService.getSolvedCount(principal.id()));
    }
}