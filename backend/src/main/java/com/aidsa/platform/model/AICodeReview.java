package com.aidsa.platform.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "ai_code_reviews",
        indexes = {
                @Index(
                        name = "idx_ai_code_reviews_submission",
                        columnList = "submission_id"
                ),
                @Index(
                        name = "idx_ai_code_reviews_created_at",
                        columnList = "created_at"
                )
        }
)
public class AICodeReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "submission_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_ai_review_submission")
    )
    private Submission submission;

    @Column(
            name = "overall_feedback",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String overallFeedback;

    @Column(
            name = "approach",
            columnDefinition = "TEXT"
    )
    private String approach;

    @Column(name = "time_complexity", length = 100)
    private String timeComplexity;

    @Column(name = "space_complexity", length = 100)
    private String spaceComplexity;

    @Column(name = "code_quality_score")
    private Integer codeQualityScore;

    @Column(
            name = "strengths",
            columnDefinition = "TEXT"
    )
    private String strengths;

    @Column(
            name = "improvements",
            columnDefinition = "TEXT"
    )
    private String improvements;

    @Column(name = "ai_provider", length = 50)
    private String aiProvider;

    @Column(name = "ai_model", length = 100)
    private String aiModel;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    public AICodeReview() {
    }

    public AICodeReview(
            Submission submission,
            String overallFeedback,
            String approach,
            String timeComplexity,
            String spaceComplexity,
            Integer codeQualityScore,
            String strengths,
            String improvements,
            String aiProvider,
            String aiModel
    ) {
        this.submission = submission;
        this.overallFeedback = overallFeedback;
        this.approach = approach;
        this.timeComplexity = timeComplexity;
        this.spaceComplexity = spaceComplexity;
        this.codeQualityScore = codeQualityScore;
        this.strengths = strengths;
        this.improvements = improvements;
        this.aiProvider = aiProvider;
        this.aiModel = aiModel;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Submission getSubmission() {
        return submission;
    }

    public void setSubmission(Submission submission) {
        this.submission = submission;
    }

    public String getOverallFeedback() {
        return overallFeedback;
    }

    public void setOverallFeedback(String overallFeedback) {
        this.overallFeedback = overallFeedback;
    }

    public String getApproach() {
        return approach;
    }

    public void setApproach(String approach) {
        this.approach = approach;
    }

    public String getTimeComplexity() {
        return timeComplexity;
    }

    public void setTimeComplexity(String timeComplexity) {
        this.timeComplexity = timeComplexity;
    }

    public String getSpaceComplexity() {
        return spaceComplexity;
    }

    public void setSpaceComplexity(String spaceComplexity) {
        this.spaceComplexity = spaceComplexity;
    }

    public Integer getCodeQualityScore() {
        return codeQualityScore;
    }

    public void setCodeQualityScore(Integer codeQualityScore) {
        this.codeQualityScore = codeQualityScore;
    }

    public String getStrengths() {
        return strengths;
    }

    public void setStrengths(String strengths) {
        this.strengths = strengths;
    }

    public String getImprovements() {
        return improvements;
    }

    public void setImprovements(String improvements) {
        this.improvements = improvements;
    }

    public String getAiProvider() {
        return aiProvider;
    }

    public void setAiProvider(String aiProvider) {
        this.aiProvider = aiProvider;
    }

    public String getAiModel() {
        return aiModel;
    }

    public void setAiModel(String aiModel) {
        this.aiModel = aiModel;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}