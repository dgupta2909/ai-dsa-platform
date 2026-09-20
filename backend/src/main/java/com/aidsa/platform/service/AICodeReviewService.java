package com.aidsa.platform.service;

import com.aidsa.platform.ai.AIReviewProvider;
import com.aidsa.platform.ai.AIReviewResult;
import com.aidsa.platform.dto.AICodeReviewResponse;
import com.aidsa.platform.exception.ResourceNotFoundException;
import com.aidsa.platform.model.AICodeReview;
import com.aidsa.platform.model.Submission;
import com.aidsa.platform.repository.AICodeReviewRepository;
import com.aidsa.platform.repository.SubmissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AICodeReviewService {

    private final AICodeReviewRepository aiCodeReviewRepository;
    private final SubmissionRepository submissionRepository;
    private final AIReviewProvider aiReviewProvider;

    public AICodeReviewService(
            AICodeReviewRepository aiCodeReviewRepository,
            SubmissionRepository submissionRepository,
            AIReviewProvider aiReviewProvider
    ) {
        this.aiCodeReviewRepository = aiCodeReviewRepository;
        this.submissionRepository = submissionRepository;
        this.aiReviewProvider = aiReviewProvider;
    }

    @Transactional
    public AICodeReviewResponse generateReview(
            Long submissionId
    ) {

        Submission submission = getSubmission(submissionId);

        AICodeReview existingReview =
                aiCodeReviewRepository
                        .findBySubmissionId(submissionId)
                        .orElse(null);

        if (existingReview != null) {
            return AICodeReviewResponse.fromEntity(
                    existingReview
            );
        }

        return createReview(submission);
    }

    @Transactional
    public AICodeReviewResponse regenerateReview(
            Long submissionId
    ) {

        Submission submission = getSubmission(submissionId);

        AICodeReview existingReview =
                aiCodeReviewRepository
                        .findBySubmissionId(submissionId)
                        .orElse(null);

        if (existingReview != null) {
            aiCodeReviewRepository.delete(existingReview);
            aiCodeReviewRepository.flush();
        }

        return createReview(submission);
    }

    private AICodeReviewResponse createReview(
            Submission submission
    ) {

        AIReviewResult result =
                aiReviewProvider.review(
                        submission.getProblem(),
                        submission
                );

        AICodeReview review = new AICodeReview(
                submission,
                result.overallFeedback(),
                result.approach(),
                result.timeComplexity(),
                result.spaceComplexity(),
                result.codeQualityScore(),
                String.join("\n", result.strengths()),
                String.join("\n", result.improvements()),
                result.provider(),
                result.model()
        );

        return AICodeReviewResponse.fromEntity(
                aiCodeReviewRepository.save(review)
        );
    }

    @Transactional(readOnly = true)
    public AICodeReviewResponse getReview(
            Long submissionId
    ) {

        AICodeReview review =
                aiCodeReviewRepository
                        .findBySubmissionId(submissionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "AI review not found for submission: "
                                                + submissionId
                                )
                        );

        return AICodeReviewResponse.fromEntity(
                review
        );
    }

    @Transactional(readOnly = true)
    public Submission getSubmission(
            Long submissionId
    ) {

        return submissionRepository
                .findById(submissionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Submission not found with id: "
                                        + submissionId
                        )
                );
    }
}