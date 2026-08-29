import { useState, useEffect } from 'react';
import { getProblemById } from '../services/problemService';

function ProblemDetailsPage({ problemId, onBackToProblems }) {
  const [problem, setProblem] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!problemId) return;
    setLoading(true);
    setError(null);
    getProblemById(problemId)
      .then((data) => setProblem(data))
      .catch((err) => setError(err.message || 'Failed to load problem details'))
      .finally(() => setLoading(false));
  }, [problemId]);

  if (loading) {
    return (
      <div className="main-content-view">
        <button type="button" className="back-btn" onClick={onBackToProblems}>
          ← Back to Problem Library
        </button>
        <div className="loading-state">Loading problem details...</div>
      </div>
    );
  }

  if (error || !problem) {
    return (
      <div className="main-content-view">
        <button type="button" className="back-btn" onClick={onBackToProblems}>
          ← Back to Problem Library
        </button>
        <div className="alert alert-error">{error || 'Problem not found'}</div>
      </div>
    );
  }

  return (
    <div className="main-content-view">
      <div className="details-header-nav">
        <button type="button" className="back-btn" onClick={onBackToProblems}>
          ← Back to Problem Library
        </button>
        <div className="problem-meta-badges">
          <span className={`difficulty-badge badge-${problem.difficulty.toLowerCase()}`}>
            {problem.difficulty}
          </span>
          <span className="category-tag">{problem.category}</span>
        </div>
      </div>

      <div className="problem-detail-card">
        <h1 className="problem-detail-title">{problem.title}</h1>

        <div className="tags-container mb-3">
          {problem.tags && problem.tags.map((tag, idx) => (
            <span key={idx} className="tag-pill">{tag}</span>
          ))}
        </div>

        <div className="problem-description">
          <h2 className="description-heading">Problem Description</h2>
          <div className="description-text">
            {problem.description.split('\n').map((line, idx) => (
              <p key={idx}>{line}</p>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

export default ProblemDetailsPage;
