package com.aidsa.platform.repository;

import com.aidsa.platform.model.AICodeReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AICodeReviewRepository
        extends JpaRepository<AICodeReview, Long> {

    Optional<AICodeReview> findBySubmissionId(Long submissionId);

    boolean existsBySubmissionId(Long submissionId);

    void deleteBySubmissionId(Long submissionId);
}