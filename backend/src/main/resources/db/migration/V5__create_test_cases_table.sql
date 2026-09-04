CREATE TABLE test_cases (
    id BIGSERIAL PRIMARY KEY,

    problem_id BIGINT NOT NULL,

    input TEXT NOT NULL,

    expected_output TEXT NOT NULL
);