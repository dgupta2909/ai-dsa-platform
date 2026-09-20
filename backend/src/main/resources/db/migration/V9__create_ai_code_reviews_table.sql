CREATE TABLE ai_code_reviews (
    id BIGSERIAL PRIMARY KEY,

    submission_id BIGINT NOT NULL UNIQUE,

    overall_feedback TEXT NOT NULL,
    approach TEXT,
    time_complexity VARCHAR(100),
    space_complexity VARCHAR(100),

    code_quality_score INTEGER
        CHECK (code_quality_score >= 0 AND code_quality_score <= 100),

    strengths TEXT,
    improvements TEXT,

    ai_provider VARCHAR(50),
    ai_model VARCHAR(100),

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ai_review_submission
        FOREIGN KEY (submission_id)
        REFERENCES submissions(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_ai_code_reviews_submission
    ON ai_code_reviews(submission_id);

CREATE INDEX idx_ai_code_reviews_created_at
    ON ai_code_reviews(created_at);