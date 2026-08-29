import { useState, useEffect } from 'react';
import { getProblemById } from '../services/problemService';
import {
  getProblemProgress,
  markProblemAttempted,
  markProblemSolved,
} from '../services/progressService';

function ProblemDetailsPage({ problemId, onBackToProblems }) {
  const [problem, setProblem] = useState(null);
  const [progress, setProgress] = useState(null);

  const [loading, setLoading] = useState(true);
  const [progressLoading, setProgressLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);

  const [error, setError] = useState(null);
  const [progressError, setProgressError] = useState(null);

  useEffect(() => {
    if (!problemId) return;

    setLoading(true);
    setError(null);

    getProblemById(problemId)
      .then((data) => setProblem(data))
      .catch((err) =>
        setError(err.message || 'Failed to load problem details')
      )
      .finally(() => setLoading(false));
  }, [problemId]);

  useEffect(() => {
    if (!problemId) return;

    setProgressLoading(true);
    setProgressError(null);

    getProblemProgress(problemId)
      .then((data) => setProgress(data))
      .catch((err) =>
        setProgressError(
          err.message || 'Failed to load problem progress'
        )
      )
      .finally(() => setProgressLoading(false));
  }, [problemId]);

  const handleAttempt = async () => {
    setActionLoading(true);
    setProgressError(null);

    try {
      const updatedProgress = await markProblemAttempted(problemId);
      setProgress(updatedProgress);
    } catch (err) {
      setProgressError(
        err.message || 'Failed to mark problem as attempted'
      );
    } finally {
      setActionLoading(false);
    }
  };

  const handleSolve = async () => {
    setActionLoading(true);
    setProgressError(null);

    try {
      const updatedProgress = await markProblemSolved(problemId);
      setProgress(updatedProgress);
    } catch (err) {
      setProgressError(
        err.message || 'Failed to mark problem as solved'
      );
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="main-content-view">
        <button
          type="button"
          className="back-btn"
          onClick={onBackToProblems}
        >
          ← Back to Problem Library
        </button>

        <div className="loading-state">
          Loading problem details...
        </div>
      </div>
    );
  }

  if (error || !problem) {
    return (
      <div className="main-content-view">
        <button
          type="button"
          className="back-btn"
          onClick={onBackToProblems}
        >
          ← Back to Problem Library
        </button>

        <div className="alert alert-error">
          {error || 'Problem not found'}
        </div>
      </div>
    );
  }

  const isSolved = progress?.status === 'SOLVED';
  const isAttempted = progress?.status === 'ATTEMPTED';

  return (
    <div className="main-content-view">
      <div className="details-header-nav">
        <button
          type="button"
          className="back-btn"
          onClick={onBackToProblems}
        >
          ← Back to Problem Library
        </button>

        <div className="problem-meta-badges">
          <span
            className={`difficulty-badge badge-${problem.difficulty.toLowerCase()}`}
          >
            {problem.difficulty}
          </span>

          <span className="category-tag">
            {problem.category}
          </span>
        </div>
      </div>

      <div className="problem-detail-card">
        <div className="problem-number">
          #{problem.problemNumber}
        </div>

        <h1 className="problem-detail-title">
          {problem.title}
        </h1>

        <div className="tags-container mb-3">
          {problem.tags &&
            problem.tags.map((tag, idx) => (
              <span key={idx} className="tag-pill">
                {tag}
              </span>
            ))}
        </div>

        {/* Progress Section */}
        <div className="problem-progress-section">
          <div className="progress-header">
            <h2 className="description-heading">
              Your Progress
            </h2>

            {!progressLoading && progress && (
              <span
                className={`progress-status ${
                  isSolved ? 'progress-solved' : 'progress-attempted'
                }`}
              >
                {isSolved ? '✓ Solved' : '◷ Attempted'}
              </span>
            )}
          </div>

          {progressError && (
            <div className="alert alert-error">
              {progressError}
            </div>
          )}

          <div className="progress-actions">
            <button
              type="button"
              className="progress-btn attempted-btn"
              onClick={handleAttempt}
              disabled={actionLoading}
            >
              {actionLoading
                ? 'Updating...'
                : isAttempted || isSolved
                  ? 'Attempt Again'
                  : 'Mark Attempted'}
            </button>

            <button
              type="button"
              className="progress-btn solved-btn"
              onClick={handleSolve}
              disabled={actionLoading || isSolved}
            >
              {isSolved ? '✓ Solved' : 'Mark as Solved'}
            </button>
          </div>

          {progress && (
            <div className="progress-info">
              <span>
                Attempts: <strong>{progress.attempts}</strong>
              </span>

              {progress.solvedAt && (
                <span>
                  Solved: <strong>Yes</strong>
                </span>
              )}
            </div>
          )}
        </div>

        <div className="problem-description">
          <h2 className="description-heading">
            Problem Description
          </h2>

          <div className="description-text">
            {problem.description.split('\n').map((line, idx) => (
              <p key={idx}>
                {line || '\u00A0'}
              </p>
            ))}
          </div>
        </div>

        <div className="problem-constraints">
          <h2 className="description-heading">
            Constraints
          </h2>

          <div className="description-text">
            {problem.constraints &&
              problem.constraints.split('\n').map((line, idx) => (
                <p key={idx}>
                  {line || '\u00A0'}
                </p>
              ))}
          </div>
        </div>
      </div>
    </div>
  );
}

export default ProblemDetailsPage;