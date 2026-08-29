CREATE TABLE user_problem_progress (
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL,
    problem_id BIGINT NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'ATTEMPTED',

    attempts INTEGER NOT NULL DEFAULT 0,
    solved_at TIMESTAMPTZ NULL,
    last_attempted_at TIMESTAMPTZ NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_progress_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_progress_problem
        FOREIGN KEY (problem_id)
        REFERENCES problems(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_progress_status
        CHECK (status IN ('ATTEMPTED', 'SOLVED')),

    CONSTRAINT chk_progress_attempts
        CHECK (attempts >= 0),

    CONSTRAINT uq_user_problem_progress
        UNIQUE (user_id, problem_id)
);

CREATE INDEX idx_progress_user
    ON user_problem_progress(user_id);

CREATE INDEX idx_progress_problem
    ON user_problem_progress(problem_id);

CREATE INDEX idx_progress_user_status
    ON user_problem_progress(user_id, status);

CREATE INDEX idx_progress_solved_at
    ON user_problem_progress(user_id, solved_at);
