import { useEffect, useMemo, useState } from 'react';
import { getProblems } from '../services/problemService';

function DashboardPage({ user, onNavigateToProblems, onSelectProblem }) {
  const [problems, setProblems] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getProblems()
      .then((data) => setProblems(Array.isArray(data) ? data : []))
      .catch(() => setProblems([]))
      .finally(() => setLoading(false));
  }, []);

  const totalProblems = problems.length;
  const solvedProblems = 0;
  const dailyGoal = 3;
  const dailyProgress = Math.min(
    Math.round((solvedProblems / dailyGoal) * 100),
    100
  );

  const recommendedProblems = problems.slice(0, 4);

  const topicStats = useMemo(() => {
    const counts = {};

    problems.forEach((problem) => {
      const category = problem.category || 'Other';
      counts[category] = (counts[category] || 0) + 1;
    });

    return Object.entries(counts)
      .sort((a, b) => b[1] - a[1])
      .slice(0, 5);
  }, [problems]);

  const firstName = user?.fullName?.split(' ')[0] || 'Developer';

  return (
    <div className="main-content-view dashboard-modern">

      {/* HERO */}
      <section className="dashboard-hero">
        <div className="hero-content">
          <div className="hero-eyebrow">
            <span className="status-dot"></span>
            YOUR DSA WORKSPACE
          </div>

          <h1 className="dashboard-hero-title">
            Welcome back, {firstName}
          </h1>

          <p className="dashboard-hero-subtitle">
            Keep building your problem-solving skills. Your next challenge
            is waiting.
          </p>

          <div className="hero-actions">
            <button
              type="button"
              className="hero-primary-btn"
              onClick={onNavigateToProblems}
            >
              Start Practicing
              <span>→</span>
            </button>

            <div className="hero-meta">
              <span className="hero-meta-icon">ST</span>
              <div>
                <strong>0 day streak</strong>
                <small>Keep the momentum going</small>
              </div>
            </div>
          </div>
        </div>

        <div className="hero-progress-card">
          <div className="progress-ring">
            <div className="progress-ring-inner">
              <strong>{dailyProgress}%</strong>
              <span>Today</span>
            </div>
          </div>

          <div className="hero-progress-info">
            <span className="progress-label">Daily Goal</span>
            <strong>{solvedProblems} / {dailyGoal} problems</strong>
            <small>Complete today's target</small>
          </div>
        </div>
      </section>

      {/* STAT CARDS */}
      <section className="dashboard-stats-grid">

        <div className="modern-stat-card">
          <div className="modern-stat-icon stat-blue">DSA</div>
          <div className="modern-stat-content">
            <span>Total Problems</span>
            <strong>{loading ? '...' : totalProblems}</strong>
            <small>Available to practice</small>
          </div>
          <div className="stat-arrow">↗</div>
        </div>

        <div className="modern-stat-card">
          <div className="modern-stat-icon stat-green">✓</div>
          <div className="modern-stat-content">
            <span>Problems Solved</span>
            <strong>{solvedProblems}</strong>
            <small>Start solving today</small>
          </div>
          <div className="stat-arrow">↗</div>
        </div>

        <div className="modern-stat-card">
          <div className="modern-stat-icon stat-orange">XP</div>
          <div className="modern-stat-content">
            <span>Current Level</span>
            <strong>1</strong>
            <small>Keep practicing to level up</small>
          </div>
          <div className="stat-arrow">↗</div>
        </div>
      </section>

      {/* MAIN GRID */}
      <section className="dashboard-main-grid">

        {/* RECOMMENDATIONS */}
        <div className="dashboard-panel recommendations-panel">
          <div className="panel-header">
            <div>
              <span className="panel-kicker">SMART PRACTICE</span>
              <h2>Recommended Problems</h2>
            </div>

            <button
              type="button"
              className="panel-link"
              onClick={onNavigateToProblems}
            >
              View all →
            </button>
          </div>

          {loading ? (
            <div className="loading-state">
              Loading recommendations...
            </div>
          ) : recommendedProblems.length === 0 ? (
            <div className="empty-state">
              No problems available currently.
            </div>
          ) : (
            <div className="modern-problem-list">
              {recommendedProblems.map((problem, index) => {
                const difficulty =
                  problem.difficulty?.toLowerCase() || 'easy';

                return (
                  <div
                    key={problem.id}
                    className="modern-problem-item"
                    onClick={() => onSelectProblem(problem.id)}
                  >
                    <div className={`problem-number number-${index + 1}`}>
                      {String(index + 1).padStart(2, '0')}
                    </div>

                    <div className="modern-problem-info">
                      <span className="modern-problem-category">
                        {problem.category || 'DSA'}
                      </span>

                      <h3>{problem.title}</h3>

                      <div className="modern-problem-tags">
                        {(Array.isArray(problem.tags)
                          ? problem.tags
                          : []
                        )
                          .slice(0, 3)
                          .map((tag, tagIndex) => (
                            <span key={tagIndex}>{tag}</span>
                          ))}
                      </div>
                    </div>

                    <div className="modern-problem-right">
                      <span className={`modern-difficulty ${difficulty}`}>
                        {problem.difficulty || 'Easy'}
                      </span>

                      <button
                        type="button"
                        className="problem-arrow-btn"
                        onClick={(event) => {
                          event.stopPropagation();
                          onSelectProblem(problem.id);
                        }}
                      >
                        →
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* DAILY GOAL */}
        <div className="dashboard-panel goal-panel">
          <div className="panel-header">
            <div>
              <span className="panel-kicker">YOUR TARGET</span>
              <h2>Today's Goal</h2>
            </div>
            <span className="goal-badge">0 / 3</span>
          </div>

          <div className="goal-visual">
            <div className="goal-circle">
              <strong>{solvedProblems}</strong>
              <span>solved</span>
            </div>
          </div>

          <div className="goal-progress">
            <div className="goal-progress-track">
              <div
                className="goal-progress-fill"
                style={{ width: `${dailyProgress}%` }}
              ></div>
            </div>

            <div className="goal-progress-labels">
              <span>0 completed</span>
              <span>{dailyGoal} target</span>
            </div>
          </div>

          <p className="goal-message">
            Solve your first problem today and start building your streak.
          </p>

          <button
            type="button"
            className="goal-btn"
            onClick={onNavigateToProblems}
          >
            Find a Problem →
          </button>
        </div>
      </section>

      {/* LOWER GRID */}
      <section className="dashboard-lower-grid">

        {/* TOPICS */}
        <div className="dashboard-panel topic-panel">
          <div className="panel-header">
            <div>
              <span className="panel-kicker">LEARNING PATH</span>
              <h2>Topics to Practice</h2>
            </div>
          </div>

          {loading ? (
            <div className="loading-state">Loading topics...</div>
          ) : topicStats.length === 0 ? (
            <div className="empty-state">No topics available.</div>
          ) : (
            <div className="topic-list">
              {topicStats.map(([topic, count]) => {
                const percentage = totalProblems
                  ? Math.round((count / totalProblems) * 100)
                  : 0;

                return (
                  <div className="topic-row" key={topic}>
                    <div className="topic-info">
                      <span>{topic}</span>
                      <strong>{count}</strong>
                    </div>

                    <div className="topic-track">
                      <div
                        className="topic-fill"
                        style={{ width: `${percentage}%` }}
                      ></div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* QUICK ACTIONS */}
        <div className="dashboard-panel quick-panel">
          <div className="panel-header">
            <div>
              <span className="panel-kicker">QUICK ACCESS</span>
              <h2>Keep Learning</h2>
            </div>
          </div>

          <button
            type="button"
            className="quick-action"
            onClick={onNavigateToProblems}
          >
            <span className="quick-icon">01</span>
            <div>
              <strong>Problem Library</strong>
              <small>Browse all available problems</small>
            </div>
            <span>→</span>
          </button>

          <button
            type="button"
            className="quick-action"
            onClick={onNavigateToProblems}
          >
            <span className="quick-icon">02</span>
            <div>
              <strong>Practice Easy Problems</strong>
              <small>Build your fundamentals</small>
            </div>
            <span>→</span>
          </button>

          <button
            type="button"
            className="quick-action"
            onClick={onNavigateToProblems}
          >
            <span className="quick-icon">03</span>
            <div>
              <strong>Challenge Yourself</strong>
              <small>Improve your interview readiness</small>
            </div>
            <span>→</span>
          </button>
        </div>
      </section>
    </div>
  );
}

export default DashboardPage;