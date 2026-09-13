import { useEffect, useMemo, useState } from 'react';
import { getProblems } from '../services/problemService';
import {
  getSubmissionHistory,
  getSubmissionAnalytics,
} from '../services/submissionService';

function DashboardPage({
  user,
  onNavigateToProblems,
  onSelectProblem,
}) {
  const [problems, setProblems] = useState([]);
  const [loading, setLoading] = useState(true);

  const [submissions, setSubmissions] = useState([]);
  const [submissionsLoading, setSubmissionsLoading] = useState(true);

  const [analytics, setAnalytics] = useState(null);
  const [analyticsLoading, setAnalyticsLoading] = useState(true);

  /* =========================================================
     LOAD PROBLEMS
     ========================================================= */

  useEffect(() => {
    getProblems()
      .then((data) => {
        setProblems(
          Array.isArray(data) ? data : []
        );
      })
      .catch(() => {
        setProblems([]);
      })
      .finally(() => {
        setLoading(false);
      });
  }, []);

  /* =========================================================
     LOAD SUBMISSION HISTORY
     ========================================================= */

  useEffect(() => {
    getSubmissionHistory()
      .then((data) => {
        setSubmissions(
          Array.isArray(data) ? data : []
        );
      })
      .catch(() => {
        setSubmissions([]);
      })
      .finally(() => {
        setSubmissionsLoading(false);
      });
  }, []);

  /* =========================================================
     LOAD ANALYTICS
     ========================================================= */

  useEffect(() => {
    const loadAnalytics = async () => {
      try {
        const data =
          await getSubmissionAnalytics();

        setAnalytics(data);
      } catch (error) {
        console.error(
          'Failed to load submission analytics:',
          error
        );

        setAnalytics(null);
      } finally {
        setAnalyticsLoading(false);
      }
    };

    loadAnalytics();
  }, []);

  /* =========================================================
     BASIC STATS
     ========================================================= */

  const totalProblems = problems.length;

  const solvedProblems = useMemo(() => {
    const solvedProblemIds = new Set();

    submissions.forEach((submission) => {
      if (
        submission.status === 'ACCEPTED' &&
        submission.problemId != null
      ) {
        solvedProblemIds.add(
          submission.problemId
        );
      }
    });

    return solvedProblemIds.size;
  }, [submissions]);

  /* =========================================================
     DAILY GOAL
     ========================================================= */

  const dailyGoal = 3;

  const todayAcceptedSubmissions =
    useMemo(() => {
      if (
        !analytics ||
        !Array.isArray(
          analytics.dailyActivity
        )
      ) {
        return 0;
      }

      const today = new Date()
        .toLocaleDateString('en-CA');

      const todayStats =
        analytics.dailyActivity.find(
          (day) => day.date === today
        );

      return todayStats?.accepted || 0;
    }, [analytics]);

  const dailySolvedCount =
    analyticsLoading
      ? 0
      : todayAcceptedSubmissions;

  const dailyProgress = Math.min(
    Math.round(
      (dailySolvedCount / dailyGoal) * 100
    ),
    100
  );

  /* =========================================================
     RECOMMENDATIONS
     ========================================================= */

  const recommendedProblems =
    problems.slice(0, 4);

  /* =========================================================
     TOPIC STATS
     ========================================================= */

  const topicStats = useMemo(() => {
    const counts = {};

    problems.forEach((problem) => {
      const category =
        problem.category || 'Other';

      counts[category] =
        (counts[category] || 0) + 1;
    });

    return Object.entries(counts)
      .sort((a, b) => b[1] - a[1])
      .slice(0, 5);
  }, [problems]);

  /* =========================================================
     RECENT SUBMISSIONS
     ========================================================= */

  const recentSubmissions =
    submissions.slice(0, 5);

  /* =========================================================
     ANALYTICS VALUES
     ========================================================= */

  const successRate =
    analytics?.successRate ?? 0;

  const totalSubmissions =
    analytics?.totalSubmissions ?? 0;

  const acceptedSubmissions =
    analytics?.acceptedSubmissions ?? 0;

  const failedSubmissions =
    analytics?.failedSubmissions ?? 0;

  const averageExecutionTime =
    analytics?.averageExecutionTimeMs;

  const fastestExecutionTime =
    analytics?.fastestExecutionTimeMs;

  const slowestExecutionTime =
    analytics?.slowestExecutionTimeMs;

  const difficultyStats =
    analytics?.difficultyStats || {};

  const dailyActivity =
    analytics?.dailyActivity || [];

  /* =========================================================
     SUBMISSION STATUS HELPERS
     ========================================================= */

  const getSubmissionStatusClass = (
    status
  ) => {
    switch (status) {
      case 'ACCEPTED':
        return 'submission-status-accepted';

      case 'WRONG_ANSWER':
        return 'submission-status-wrong';

      case 'COMPILATION_ERROR':
        return 'submission-status-error';

      case 'RUNTIME_ERROR':
        return 'submission-status-error';

      case 'TIME_LIMIT_EXCEEDED':
        return 'submission-status-timeout';

      default:
        return 'submission-status-default';
    }
  };

  const getSubmissionStatusLabel = (
    status
  ) => {
    switch (status) {
      case 'ACCEPTED':
        return 'Accepted';

      case 'WRONG_ANSWER':
        return 'Wrong Answer';

      case 'COMPILATION_ERROR':
        return 'Compilation Error';

      case 'RUNTIME_ERROR':
        return 'Runtime Error';

      case 'TIME_LIMIT_EXCEEDED':
        return 'Time Limit Exceeded';

      default:
        return status || 'Unknown';
    }
  };

  /* =========================================================
     SUBMISSION TIME FORMATTER
     ========================================================= */

  const formatSubmissionTime = (
    createdAt
  ) => {
    if (!createdAt) {
      return 'Unknown time';
    }

    const date = new Date(createdAt);

    if (Number.isNaN(date.getTime())) {
      return 'Unknown time';
    }

    const now = new Date();

    const diffMs =
      now.getTime() -
      date.getTime();

    const diffMinutes = Math.floor(
      diffMs / (1000 * 60)
    );

    const diffHours = Math.floor(
      diffMs / (1000 * 60 * 60)
    );

    const diffDays = Math.floor(
      diffMs / (1000 * 60 * 60 * 24)
    );

    if (diffMinutes < 1) {
      return 'Just now';
    }

    if (diffMinutes < 60) {
      return `${diffMinutes} min ago`;
    }

    if (diffHours < 24) {
      return `${diffHours} hr ago`;
    }

    if (diffDays < 7) {
      return `${diffDays} day${
        diffDays === 1 ? '' : 's'
      } ago`;
    }

    return date.toLocaleDateString();
  };

  /* =========================================================
     DIFFICULTY HELPERS
     ========================================================= */

  const getDifficultyData = (
    difficulty
  ) => {
    return (
      difficultyStats[difficulty] || {
        submissions: 0,
        accepted: 0,
        failed: 0,
        successRate: 0,
      }
    );
  };

  const firstName =
    user?.fullName?.split(' ')[0] ||
    'Developer';

  return (
    <div className="main-content-view dashboard-modern">

      {/* =====================================================
          HERO
          ===================================================== */}

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
            Keep building your problem-solving
            skills. Your next challenge is waiting.
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

              <span className="hero-meta-icon">
                ST
              </span>

              <div>
                <strong>
                  0 day streak
                </strong>

                <small>
                  Keep the momentum going
                </small>
              </div>

            </div>

          </div>

        </div>

        <div className="hero-progress-card">

          <div className="progress-ring">

            <div className="progress-ring-inner">

              <strong>
                {analyticsLoading
                  ? '...'
                  : `${dailyProgress}%`}
              </strong>

              <span>
                Today
              </span>

            </div>

          </div>

          <div className="hero-progress-info">

            <span className="progress-label">
              Daily Goal
            </span>

            <strong>
              {analyticsLoading
                ? '...'
                : `${Math.min(
                    dailySolvedCount,
                    dailyGoal
                  )} / ${dailyGoal} problems`}
            </strong>

            <small>
              Complete today's target
            </small>

          </div>

        </div>

      </section>

      {/* =====================================================
          STAT CARDS
          ===================================================== */}

      <section className="dashboard-stats-grid">

        <div className="modern-stat-card">

          <div className="modern-stat-icon stat-blue">
            DSA
          </div>

          <div className="modern-stat-content">

            <span>
              Total Problems
            </span>

            <strong>
              {loading
                ? '...'
                : totalProblems}
            </strong>

            <small>
              Available to practice
            </small>

          </div>

          <div className="stat-arrow">
            ↗
          </div>

        </div>

        <div className="modern-stat-card">

          <div className="modern-stat-icon stat-green">
            ✓
          </div>

          <div className="modern-stat-content">

            <span>
              Problems Solved
            </span>

            <strong>
              {submissionsLoading
                ? '...'
                : solvedProblems}
            </strong>

            <small>
              Successfully completed
            </small>

          </div>

          <div className="stat-arrow">
            ↗
          </div>

        </div>

        <div className="modern-stat-card">

          <div className="modern-stat-icon stat-orange">
            XP
          </div>

          <div className="modern-stat-content">

            <span>
              Current Level
            </span>

            <strong>
              1
            </strong>

            <small>
              Keep practicing to level up
            </small>

          </div>

          <div className="stat-arrow">
            ↗
          </div>

        </div>

      </section>

      {/* =====================================================
          MAIN GRID
          ===================================================== */}

      <section className="dashboard-main-grid">

        {/* RECOMMENDATIONS */}

        <div className="dashboard-panel recommendations-panel">

          <div className="panel-header">

            <div>

              <span className="panel-kicker">
                SMART PRACTICE
              </span>

              <h2>
                Recommended Problems
              </h2>

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

              {recommendedProblems.map(
                (problem, index) => {

                  const difficulty =
                    problem.difficulty
                      ?.toLowerCase() ||
                    'easy';

                  return (

                    <div
                      key={problem.id}
                      className="modern-problem-item"
                      onClick={() =>
                        onSelectProblem(
                          problem.id
                        )
                      }
                    >

                      <div
                        className={`problem-number number-${
                          index + 1
                        }`}
                      >
                        {String(
                          index + 1
                        ).padStart(2, '0')}
                      </div>

                      <div className="modern-problem-info">

                        <span className="modern-problem-category">
                          {problem.category ||
                            'DSA'}
                        </span>

                        <h3>
                          {problem.title}
                        </h3>

                        <div className="modern-problem-tags">

                          {(
                            Array.isArray(
                              problem.tags
                            )
                              ? problem.tags
                              : []
                          )
                            .slice(0, 3)
                            .map(
                              (
                                tag,
                                tagIndex
                              ) => (
                                <span
                                  key={
                                    tagIndex
                                  }
                                >
                                  {tag}
                                </span>
                              )
                            )}

                        </div>

                      </div>

                      <div className="modern-problem-right">

                        <span
                          className={`modern-difficulty ${difficulty}`}
                        >
                          {problem.difficulty ||
                            'Easy'}
                        </span>

                        <button
                          type="button"
                          className="problem-arrow-btn"
                          onClick={(
                            event
                          ) => {
                            event.stopPropagation();

                            onSelectProblem(
                              problem.id
                            );
                          }}
                        >
                          →
                        </button>

                      </div>

                    </div>
                  );
                }
              )}

            </div>
          )}

        </div>

        {/* DAILY GOAL */}

        <div className="dashboard-panel goal-panel">

          <div className="panel-header">

            <div>

              <span className="panel-kicker">
                YOUR TARGET
              </span>

              <h2>
                Today's Goal
              </h2>

            </div>

            <span className="goal-badge">

              {analyticsLoading
                ? '...'
                : `${Math.min(
                    dailySolvedCount,
                    dailyGoal
                  )} / ${dailyGoal}`}

            </span>

          </div>

          <div className="goal-visual">

            <div className="goal-circle">

              <strong>
                {analyticsLoading
                  ? '...'
                  : Math.min(
                      dailySolvedCount,
                      dailyGoal
                    )}
              </strong>

              <span>
                solved
              </span>

            </div>

          </div>

          <div className="goal-progress">

            <div className="goal-progress-track">

              <div
                className="goal-progress-fill"
                style={{
                  width: `${dailyProgress}%`,
                }}
              ></div>

            </div>

            <div className="goal-progress-labels">

              <span>
                {analyticsLoading
                  ? 'Loading...'
                  : `${Math.min(
                      dailySolvedCount,
                      dailyGoal
                    )} completed`}
              </span>

              <span>
                {dailyGoal} target
              </span>

            </div>

          </div>

          <p className="goal-message">

            {analyticsLoading
              ? "Loading today's progress..."
              : dailySolvedCount >= dailyGoal
                ? "Today's goal is complete. Great work!"
                : 'Solve your next problem and keep building your streak.'}

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

      {/* =====================================================
          PERFORMANCE ANALYTICS
          ===================================================== */}

      <section className="dashboard-panel analytics-panel">

        <div className="panel-header analytics-panel-header">

          <div>

            <span className="panel-kicker">
              PERFORMANCE
            </span>

            <h2>
              Your Analytics
            </h2>

            <p className="analytics-subtitle">
              Understand your solving performance
              and improve where it matters.
            </p>

          </div>

          <div className="analytics-live-badge">
            <span></span>
            LIVE DATA
          </div>

        </div>

        {analyticsLoading ? (

          <div className="analytics-loading">
            <div className="analytics-loading-spinner"></div>

            <div>
              <strong>
                Loading performance data
              </strong>

              <span>
                Analyzing your submissions...
              </span>
            </div>
          </div>

        ) : !analytics ? (

          <div className="analytics-empty">
            <div className="analytics-empty-icon">
              —
            </div>

            <div>
              <strong>
                Analytics unavailable
              </strong>

              <span>
                Your performance data could not
                be loaded right now.
              </span>
            </div>
          </div>

        ) : (

          <>
            {/* ANALYTICS OVERVIEW */}

            <div className="analytics-overview-grid">

              <div className="analytics-metric-card analytics-success-card">

                <div className="analytics-metric-top">
                  <span>
                    Success Rate
                  </span>

                  <div className="analytics-metric-icon">
                    %
                  </div>
                </div>

                <strong>
                  {successRate.toFixed(1)}%
                </strong>

                <small>
                  {acceptedSubmissions} accepted
                  out of {totalSubmissions}
                </small>

                <div className="analytics-mini-track">
                  <div
                    style={{
                      width: `${Math.min(
                        successRate,
                        100
                      )}%`,
                    }}
                  ></div>
                </div>

              </div>

              <div className="analytics-metric-card">

                <div className="analytics-metric-top">

                  <span>
                    Total Submissions
                  </span>

                  <div className="analytics-metric-icon">
                    #
                  </div>

                </div>

                <strong>
                  {totalSubmissions}
                </strong>

                <small>
                  Attempts recorded
                </small>

              </div>

              <div className="analytics-metric-card">

                <div className="analytics-metric-top">

                  <span>
                    Accepted
                  </span>

                  <div className="analytics-metric-icon analytics-icon-success">
                    ✓
                  </div>

                </div>

                <strong>
                  {acceptedSubmissions}
                </strong>

                <small>
                  Successful submissions
                </small>

              </div>

              <div className="analytics-metric-card">

                <div className="analytics-metric-top">

                  <span>
                    Failed
                  </span>

                  <div className="analytics-metric-icon analytics-icon-failed">
                    ×
                  </div>

                </div>

                <strong>
                  {failedSubmissions}
                </strong>

                <small>
                  Attempts needing improvement
                </small>

              </div>

            </div>

            {/* SECOND ANALYTICS ROW */}

            <div className="analytics-secondary-grid">

              {/* EXECUTION PERFORMANCE */}

              <div className="analytics-sub-panel">

                <div className="analytics-sub-panel-header">

                  <div>

                    <span>
                      EXECUTION
                    </span>

                    <h3>
                      Runtime Performance
                    </h3>

                  </div>

                  <div className="analytics-runtime-icon">
                    ⚡
                  </div>

                </div>

                <div className="runtime-stats">

                  <div className="runtime-stat">

                    <span>
                      Average
                    </span>

                    <strong>
                      {averageExecutionTime !=
                      null
                        ? `${averageExecutionTime} ms`
                        : '—'}
                    </strong>

                  </div>

                  <div className="runtime-divider"></div>

                  <div className="runtime-stat">

                    <span>
                      Fastest
                    </span>

                    <strong>
                      {fastestExecutionTime !=
                      null
                        ? `${fastestExecutionTime} ms`
                        : '—'}
                    </strong>

                  </div>

                  <div className="runtime-divider"></div>

                  <div className="runtime-stat">

                    <span>
                      Slowest
                    </span>

                    <strong>
                      {slowestExecutionTime !=
                      null
                        ? `${slowestExecutionTime} ms`
                        : '—'}
                    </strong>

                  </div>

                </div>

              </div>

              {/* DIFFICULTY PERFORMANCE */}

              <div className="analytics-sub-panel">

                <div className="analytics-sub-panel-header">

                  <div>

                    <span>
                      DIFFICULTY
                    </span>

                    <h3>
                      Performance by Level
                    </h3>

                  </div>

                  <div className="analytics-difficulty-icon">
                    DSA
                  </div>

                </div>

                <div className="difficulty-performance-list">

                  {[
                    ['EASY', 'Easy'],
                    ['MEDIUM', 'Medium'],
                    ['HARD', 'Hard'],
                  ].map(
                    ([key, label]) => {

                      const stats =
                        getDifficultyData(
                          key
                        );

                      return (

                        <div
                          className="difficulty-performance-row"
                          key={key}
                        >

                          <div className="difficulty-performance-info">

                            <span
                              className={`difficulty-dot ${key.toLowerCase()}`}
                            ></span>

                            <strong>
                              {label}
                            </strong>

                            <small>
                              {stats.accepted}/
                              {stats.submissions}
                            </small>

                          </div>

                          <div className="difficulty-performance-bar">

                            <div className="difficulty-performance-track">

                              <div
                                className={`difficulty-performance-fill ${key.toLowerCase()}`}
                                style={{
                                  width: `${Math.min(
                                    stats.successRate ||
                                      0,
                                    100
                                  )}%`,
                                }}
                              ></div>

                            </div>

                            <strong>
                              {(
                                stats.successRate ||
                                0
                              ).toFixed(0)}
                              %
                            </strong>

                          </div>

                        </div>
                      );
                    }
                  )}

                </div>

              </div>

            </div>

            {/* SEVEN DAY ACTIVITY */}

            <div className="analytics-activity-panel">

              <div className="analytics-activity-header">

                <div>

                  <span>
                    ACTIVITY
                  </span>

                  <h3>
                    Last 7 Days
                  </h3>

                </div>

                <div className="activity-legend">

                  <span>
                    <i className="legend-total"></i>
                    Submissions
                  </span>

                  <span>
                    <i className="legend-accepted"></i>
                    Accepted
                  </span>

                </div>

              </div>

              <div className="activity-chart">

                {dailyActivity.map(
                  (day) => {

                    const maxValue =
                      Math.max(
                        ...dailyActivity.map(
                          (item) =>
                            item.submissions
                        ),
                        1
                      );

                    const totalHeight =
                      Math.max(
                        (day.submissions /
                          maxValue) *
                          100,
                        day.submissions > 0
                          ? 8
                          : 3
                      );

                    const acceptedHeight =
                      day.submissions > 0
                        ? Math.max(
                            (day.accepted /
                              maxValue) *
                              100,
                            day.accepted > 0
                              ? 8
                              : 3
                          )
                        : 3;

                    const date =
                      new Date(
                        `${day.date}T00:00:00`
                      );

                    const dayLabel =
                      date.toLocaleDateString(
                        'en-US',
                        {
                          weekday: 'short',
                        }
                      );

                    return (

                      <div
                        className="activity-day"
                        key={day.date}
                      >

                        <div className="activity-bars">

                          <div
                            className="activity-bar total"
                            style={{
                              height: `${totalHeight}%`,
                            }}
                            title={`${day.submissions} submissions`}
                          ></div>

                          <div
                            className="activity-bar accepted"
                            style={{
                              height: `${acceptedHeight}%`,
                            }}
                            title={`${day.accepted} accepted`}
                          ></div>

                        </div>

                        <strong>
                          {day.submissions}
                        </strong>

                        <span>
                          {dayLabel}
                        </span>

                      </div>
                    );
                  }
                )}

              </div>

            </div>
          </>
        )}

      </section>

      {/* =====================================================
          SUBMISSION HISTORY
          ===================================================== */}

      <section className="dashboard-panel submission-history-panel">

        <div className="panel-header">

          <div>

            <span className="panel-kicker">
              RECENT ACTIVITY
            </span>

            <h2>
              Submission History
            </h2>

          </div>

          {submissions.length > 0 && (

            <span className="submission-count-badge">

              {submissions.length} submission
              {submissions.length === 1
                ? ''
                : 's'}

            </span>

          )}

        </div>

        {submissionsLoading ? (

          <div className="loading-state">
            Loading submission history...
          </div>

        ) : recentSubmissions.length === 0 ? (

          <div className="submission-empty-state">

            <div className="submission-empty-icon">
              ⌁
            </div>

            <div>

              <strong>
                No submissions yet
              </strong>

              <p>
                Submit your first solution to
                start tracking your progress.
              </p>

            </div>

            <button
              type="button"
              className="submission-empty-btn"
              onClick={onNavigateToProblems}
            >
              Start Practicing →
            </button>

          </div>

        ) : (

          <div className="submission-history-list">

            {recentSubmissions.map(
              (submission) => (

                <div
                  key={submission.id}
                  className="submission-history-item"
                >

                  <div className="submission-history-left">

                    <div
                      className={`submission-status-indicator ${getSubmissionStatusClass(
                        submission.status
                      )}`}
                    >
                      {submission.status ===
                      'ACCEPTED'
                        ? '✓'
                        : submission.status ===
                          'WRONG_ANSWER'
                          ? '×'
                          : submission.status ===
                            'TIME_LIMIT_EXCEEDED'
                            ? '⏱'
                            : '•'}
                    </div>

                    <div className="submission-history-info">

                      <strong>
                        Problem #
                        {submission.problemId}
                      </strong>

                      <div className="submission-history-meta">

                        <span>
                          {submission.language ||
                            'Unknown'}
                        </span>

                        <span>
                          •
                        </span>

                        <span>
                          {formatSubmissionTime(
                            submission.createdAt
                          )}
                        </span>

                      </div>

                    </div>

                  </div>

                  <div className="submission-history-right">

                    <span
                      className={`submission-status-badge ${getSubmissionStatusClass(
                        submission.status
                      )}`}
                    >
                      {getSubmissionStatusLabel(
                        submission.status
                      )}
                    </span>

                    <span className="submission-execution-time">

                      {submission.executionTimeMs !=
                      null
                        ? `${submission.executionTimeMs} ms`
                        : '—'}

                    </span>

                  </div>

                </div>
              )
            )}

          </div>
        )}

      </section>

      {/* =====================================================
          LOWER GRID
          ===================================================== */}

      <section className="dashboard-lower-grid">

        {/* TOPICS */}

        <div className="dashboard-panel topic-panel">

          <div className="panel-header">

            <div>

              <span className="panel-kicker">
                LEARNING PATH
              </span>

              <h2>
                Topics to Practice
              </h2>

            </div>

          </div>

          {loading ? (

            <div className="loading-state">
              Loading topics...
            </div>

          ) : topicStats.length === 0 ? (

            <div className="empty-state">
              No topics available.
            </div>

          ) : (

            <div className="topic-list">

              {topicStats.map(
                ([topic, count]) => {

                  const percentage =
                    totalProblems
                      ? Math.round(
                          (count /
                            totalProblems) *
                            100
                        )
                      : 0;

                  return (

                    <div
                      className="topic-row"
                      key={topic}
                    >

                      <div className="topic-info">

                        <span>
                          {topic}
                        </span>

                        <strong>
                          {count}
                        </strong>

                      </div>

                      <div className="topic-track">

                        <div
                          className="topic-fill"
                          style={{
                            width: `${percentage}%`,
                          }}
                        ></div>

                      </div>

                    </div>
                  );
                }
              )}

            </div>
          )}

        </div>

        {/* QUICK ACTIONS */}

        <div className="dashboard-panel quick-panel">

          <div className="panel-header">

            <div>

              <span className="panel-kicker">
                QUICK ACCESS
              </span>

              <h2>
                Keep Learning
              </h2>

            </div>

          </div>

          <button
            type="button"
            className="quick-action"
            onClick={onNavigateToProblems}
          >

            <span className="quick-icon">
              01
            </span>

            <div>

              <strong>
                Problem Library
              </strong>

              <small>
                Browse all available problems
              </small>

            </div>

            <span>
              →
            </span>

          </button>

          <button
            type="button"
            className="quick-action"
            onClick={onNavigateToProblems}
          >

            <span className="quick-icon">
              02
            </span>

            <div>

              <strong>
                Practice Easy Problems
              </strong>

              <small>
                Build your fundamentals
              </small>

            </div>

            <span>
              →
            </span>

          </button>

          <button
            type="button"
            className="quick-action"
            onClick={onNavigateToProblems}
          >

            <span className="quick-icon">
              03
            </span>

            <div>

              <strong>
                Challenge Yourself
              </strong>

              <small>
                Improve your interview readiness
              </small>

            </div>

            <span>
              →
            </span>

          </button>

        </div>

      </section>

    </div>
  );
}

export default DashboardPage;