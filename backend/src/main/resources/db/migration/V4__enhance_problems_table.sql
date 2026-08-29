ALTER TABLE problems
    ADD COLUMN IF NOT EXISTS problem_number INTEGER;

ALTER TABLE problems
    ADD COLUMN IF NOT EXISTS constraints TEXT;

UPDATE problems
SET problem_number = id
WHERE problem_number IS NULL;

UPDATE problems
SET constraints = 'Constraints will be added.'
WHERE constraints IS NULL;

ALTER TABLE problems
    ALTER COLUMN problem_number SET NOT NULL;

ALTER TABLE problems
    ALTER COLUMN constraints SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_problems_problem_number
    ON problems(problem_number);