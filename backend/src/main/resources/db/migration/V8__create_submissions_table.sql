CREATE TABLE submissions (
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL,
    problem_id BIGINT NOT NULL,

    language VARCHAR(20) NOT NULL,
    code TEXT NOT NULL,

    status VARCHAR(30) NOT NULL,

    execution_time_ms BIGINT,

    error_message TEXT,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_submissions_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_submissions_problem
        FOREIGN KEY (problem_id)
        REFERENCES problems(id)
        ON DELETE CASCADE
);