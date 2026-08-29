package com.aidsa.platform.service;

import com.aidsa.platform.dto.ProblemDetailResponse;
import com.aidsa.platform.dto.ProblemSummaryResponse;
import com.aidsa.platform.exception.ResourceNotFoundException;
import com.aidsa.platform.model.Difficulty;
import com.aidsa.platform.model.Problem;
import com.aidsa.platform.repository.ProblemRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProblemService {

    private final ProblemRepository problemRepository;

    public ProblemService(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }

    @Transactional(readOnly = true)
    public List<ProblemSummaryResponse> getProblems(Difficulty difficulty, String category, String search) {
        Specification<Problem> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (difficulty != null) {
                predicates.add(cb.equal(root.get("difficulty"), difficulty));
            }

            if (category != null && !category.trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("category")), category.trim().toLowerCase()));
            }

            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate titleLike = cb.like(cb.lower(root.get("title")), pattern);
                Predicate tagsLike = cb.like(cb.lower(root.get("tags")), pattern);
                predicates.add(cb.or(titleLike, tagsLike));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<Problem> problems = problemRepository.findAll(spec);
        return problems.stream()
                .map(ProblemSummaryResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProblemDetailResponse getProblemById(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + id));
        return ProblemDetailResponse.fromEntity(problem);
    }

    @Transactional(readOnly = true)
    public ProblemDetailResponse getProblemBySlug(String slug) {
        Problem problem = problemRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with slug: " + slug));
        return ProblemDetailResponse.fromEntity(problem);
    }

    @Transactional(readOnly = true)
    public long getTotalProblemCount() {
        return problemRepository.count();
    }
}
