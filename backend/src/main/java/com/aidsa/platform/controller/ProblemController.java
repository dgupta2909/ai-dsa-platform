package com.aidsa.platform.controller;

import com.aidsa.platform.dto.ProblemDetailResponse;
import com.aidsa.platform.dto.ProblemSummaryResponse;
import com.aidsa.platform.model.Difficulty;
import com.aidsa.platform.service.ProblemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @GetMapping
    public ResponseEntity<List<ProblemSummaryResponse>> getProblems(
            @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search
    ) {
        List<ProblemSummaryResponse> problems = problemService.getProblems(difficulty, category, search);
        return ResponseEntity.ok(problems);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProblemDetailResponse> getProblemById(@PathVariable Long id) {
        ProblemDetailResponse problem = problemService.getProblemById(id);
        return ResponseEntity.ok(problem);
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ProblemDetailResponse> getProblemBySlug(@PathVariable String slug) {
        ProblemDetailResponse problem = problemService.getProblemBySlug(slug);
        return ResponseEntity.ok(problem);
    }
}
