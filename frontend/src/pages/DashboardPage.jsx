import { useState, useEffect } from 'react';
import { getProblems } from '../services/problemService';

function DashboardPage({ user, onNavigateToProblems, onSelectProblem }) {
  const [problems, setProblems] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getProblems()
      .then((data) => setProblems(data || []))
      .catch(() => setProblems([]))
      .finally(() => setLoading(false));
  }, []);

  const totalProblems = problems.length;
  const recommendedProblems = problems.slice(0, 4);

  return (
    <div className="main-content-view">

      <div className="view-header">
        <div>
          <h1 className="view-title">
            Welcome back, {user.fullName}
          </h1>

          <p className="view-subtitle">
            Continue your DSA preparation and build your interview skills.
          </p>
        </div>

        <button
          type="button"
          className="primary-btn"
          onClick={onNavigateToProblems}
        >
          Start Practicing
        </button>
      </div>

      <div className="stats-grid">

        <div className="stat-card">
          <div className="stat-icon-wrapper blue-icon">
            DSA
          </div>

          <div className="stat-info">
            <span className="stat-label">
              Total Problems
            </span>

            <span className="stat-number">
              {loading ? '...' : totalProblems}
            </span>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon-wrapper green-icon">
            ✓
          </div>

          <div className="stat-info">
            <span className="stat-label">
              Problems Solved
            </span>

            <span className="stat-number">
              0
            </span>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon-wrapper amber-icon">
            ST
          </div>

          <div className="stat-info">
            <span className="stat-label">
              Current Streak
            </span>

            <span className="stat-number">
              0 Days
            </span>
          </div>
        </div>

      </div>

      <div className="dashboard-section">

        <div className="section-header">
          <h2 className="section-title">
            Recommended Problems
          </h2>

          <button
            type="button"
            className="secondary-btn"
            onClick={onNavigateToProblems}
          >
            View All Problems
          </button>
        </div>

        {loading ? (
          <div className="loading-state">
            Loading recommended problems...
          </div>
        ) : recommendedProblems.length === 0 ? (
          <div className="empty-state">
            No problems available currently.
          </div>
        ) : (
          <div className="problem-cards-grid">

            {recommendedProblems.map((problem) => (

              <div
                key={problem.id}
                className="problem-card"
                onClick={() => onSelectProblem(problem.id)}
              >

                <div className="problem-card-top">

                  <span className="problem-card-category">
                    {problem.category}
                  </span>

                  <span
                    className={`difficulty-badge badge-${problem.difficulty.toLowerCase()}`}
                  >
                    {problem.difficulty}
                  </span>

                </div>

                <h3 className="problem-card-title">
                  {problem.title}
                </h3>

                <div className="problem-card-tags">

                  {problem.tags.map((tag, index) => (
                    <span
                      key={index}
                      className="tag-pill"
                    >
                      {tag}
                    </span>
                  ))}

                </div>

                <button
                  type="button"
                  className="solve-btn"
                  onClick={(event) => {
                    event.stopPropagation();
                    onSelectProblem(problem.id);
                  }}
                >
                  Solve Problem →
                </button>

              </div>

            ))}

          </div>
        )}

      </div>

    </div>
  );
}

export default DashboardPage;