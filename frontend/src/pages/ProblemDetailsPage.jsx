import { useEffect, useState } from 'react';
import Editor from '@monaco-editor/react';
import { getProblemById } from '../services/problemService';
import {
  markProblemAttempted,
  markProblemSolved,
} from '../services/progressService';
import { runCode } from '../services/codeService';
import { submitCode } from '../services/submissionService';

/* =========================================================
   STARTER CODE
   ========================================================= */

const starterCode = {
  java:
    'import java.util.*;\n\npublic class Solution {\n    public static void main(String[] args) {\n\n        // Write your solution here\n\n    }\n}',

  javascript:
    'function solution() {\n\n    // Write your solution here\n\n}\n\nconsole.log(solution());',

  python:
    'def solution():\n\n    # Write your solution here\n    pass\n\n\nif __name__ == "__main__":\n    solution()',

  cpp:
    '#include <bits/stdc++.h>\nusing namespace std;\n\nint main() {\n\n    // Write your solution here\n\n    return 0;\n}',
};

/* =========================================================
   LANGUAGE DISPLAY NAMES
   ========================================================= */

const languageNames = {
  java: 'Java',
  javascript: 'JavaScript',
  python: 'Python',
  cpp: 'C++',
};

/* =========================================================
   SUBMISSION STATUS HELPERS
   ========================================================= */

const submissionStatusConfig = {
  ACCEPTED: {
    title: 'Accepted',
    subtitle: 'Your solution passed all test cases.',
    icon: '✓',
    className: 'accepted',
  },

  WRONG_ANSWER: {
    title: 'Wrong Answer',
    subtitle: 'Your solution did not produce the expected output.',
    icon: '×',
    className: 'wrong-answer',
  },

  RUNTIME_ERROR: {
    title: 'Runtime Error',
    subtitle: 'Your program encountered an error while running.',
    icon: '!',
    className: 'runtime-error',
  },

  COMPILATION_ERROR: {
    title: 'Compilation Error',
    subtitle: 'Your code could not be compiled.',
    icon: '!',
    className: 'compilation-error',
  },

  TIME_LIMIT_EXCEEDED: {
    title: 'Time Limit Exceeded',
    subtitle: 'Your solution took longer than the allowed limit.',
    icon: '⏱',
    className: 'time-limit',
  },

  PENDING: {
    title: 'Pending',
    subtitle: 'Your submission is being processed.',
    icon: '…',
    className: 'pending',
  },
};

function getSubmissionConfig(status) {
  return (
    submissionStatusConfig[status] || {
      title: status || 'Submission Result',
      subtitle: 'Your submission has been processed.',
      icon: '•',
      className: 'unknown',
    }
  );
}

/* =========================================================
   MAIN COMPONENT
   ========================================================= */

function ProblemDetailsPage({ problemId, onBackToProblems }) {
  const [problem, setProblem] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  /* =======================================================
     LANGUAGE
     ======================================================= */

  const [language, setLanguage] = useState('java');

  /* =======================================================
     CODE STORAGE
     ======================================================= */

  const [codes, setCodes] = useState({
    java: starterCode.java,
    javascript: starterCode.javascript,
    python: starterCode.python,
    cpp: starterCode.cpp,
  });

  /* =======================================================
     EXECUTION / SUBMISSION STATE
     ======================================================= */

  const [output, setOutput] = useState('');
  const [isRunning, setIsRunning] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [submissionResult, setSubmissionResult] = useState(null);

  /* =======================================================
     PROGRESS STATE
     ======================================================= */

  const [progressMessage, setProgressMessage] = useState('');
  const [progressError, setProgressError] = useState('');

  /* =======================================================
     LOAD PROBLEM
     ======================================================= */

  useEffect(() => {
    if (!problemId) {
      setLoading(false);
      setProblem(null);
      return;
    }

    setLoading(true);
    setError(null);

    getProblemById(problemId)
      .then((data) => {
        setProblem(data);
      })
      .catch((err) => {
        setError(
          err.message || 'Failed to load problem details'
        );
      })
      .finally(() => {
        setLoading(false);
      });
  }, [problemId]);

  /* =======================================================
     CURRENT CODE
     ======================================================= */

  const currentCode = codes[language];

  /* =======================================================
     HANDLE CODE CHANGE
     ======================================================= */

  const handleCodeChange = (value) => {
    setCodes((previousCodes) => ({
      ...previousCodes,
      [language]: value || '',
    }));
  };

  /* =======================================================
     HANDLE LANGUAGE CHANGE
     ======================================================= */

  const handleLanguageChange = (event) => {
    const newLanguage = event.target.value;

    setLanguage(newLanguage);

    setOutput('');
    setSubmissionResult(null);
    setProgressMessage('');
    setProgressError('');
  };

  /* =======================================================
     RESET CURRENT LANGUAGE CODE
     ======================================================= */

  const handleResetCode = () => {
    const confirmed = window.confirm(
      `Reset your ${languageNames[language]} code to the default template?`
    );

    if (!confirmed) {
      return;
    }

    setCodes((previousCodes) => ({
      ...previousCodes,
      [language]: starterCode[language],
    }));

    setOutput('');
    setSubmissionResult(null);
  };

  /* =======================================================
     MARK ATTEMPTED
     ======================================================= */

  const handleAttempted = async () => {
    try {
      setProgressError('');
      setProgressMessage('');

      await markProblemAttempted(problemId);

      setProgressMessage(
        'Problem marked as attempted.'
      );
    } catch (err) {
      setProgressError(
        err.message ||
          'Failed to mark problem as attempted'
      );
    }
  };

  /* =======================================================
     MARK SOLVED
     ======================================================= */

  const handleSolved = async () => {
    try {
      setProgressError('');
      setProgressMessage('');

      await markProblemSolved(problemId);

      setProgressMessage(
        'Problem marked as solved!'
      );
    } catch (err) {
      setProgressError(
        err.message ||
          'Failed to mark problem as solved'
      );
    }
  };

  /* =======================================================
     RUN CODE
     ======================================================= */

  const handleRunCode = async () => {
    if (!currentCode || !currentCode.trim()) {
      setOutput('Code cannot be empty.');
      setSubmissionResult(null);
      return;
    }

    setIsRunning(true);
    setOutput('');
    setSubmissionResult(null);

    try {
      const response = await runCode(
        language,
        currentCode
      );

      if (response.success) {
        setOutput(
          response.output ||
            'Program executed successfully.'
        );
      } else {
        const errorMessage =
          response.error ||
          'Code execution failed.';

        const outputMessage = response.output
          ? `${response.output}\n\n${errorMessage}`
          : errorMessage;

        setOutput(outputMessage);
      }
    } catch (err) {
      setOutput(
        err.message ||
          'Unable to connect to the code execution server.'
      );
    } finally {
      setIsRunning(false);
    }
  };

  /* =======================================================
     SUBMIT CODE
     ======================================================= */

  const handleSubmitCode = async () => {
    if (!currentCode || !currentCode.trim()) {
      setOutput('Code cannot be empty.');
      setSubmissionResult(null);
      return;
    }

    setIsSubmitting(true);
    setOutput('');
    setSubmissionResult(null);

    try {
      const response = await submitCode(
        problemId,
        language,
        currentCode
      );

      setSubmissionResult(response);
    } catch (err) {
      setSubmissionResult({
        status: 'SUBMISSION_ERROR',
        errorMessage:
          err.message ||
          'Unable to connect to the submission server.',
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  /* =======================================================
     LOADING
     ======================================================= */

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

  /* =======================================================
     ERROR
     ======================================================= */

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

  const difficulty =
    problem.difficulty || 'Unknown';

  const difficultyClass =
    difficulty.toLowerCase();

  /* =======================================================
     MAIN PAGE
     ======================================================= */

  return (
    <div className="main-content-view problem-details-page">

      {/* =====================================================
          TOP NAVIGATION
         ===================================================== */}

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
            className={`difficulty-badge badge-${difficultyClass}`}
          >
            {difficulty}
          </span>

          {problem.category && (
            <span className="category-tag">
              {problem.category}
            </span>
          )}

        </div>

      </div>

      {/* =====================================================
          PROBLEM INFORMATION
         ===================================================== */}

      <div className="problem-detail-card">

        <div className="problem-number">
          #{problem.problemNumber}
        </div>

        <h1 className="problem-detail-title">
          {problem.title}
        </h1>

        {problem.tags &&
          problem.tags.length > 0 && (
            <div className="tags-container">
              {problem.tags.map((tag, idx) => (
                <span
                  key={`${tag}-${idx}`}
                  className="tag-pill"
                >
                  {tag}
                </span>
              ))}
            </div>
          )}

        {/* Problem Description */}

        <div className="problem-description">

          <h2 className="description-heading">
            Problem Description
          </h2>

          <div className="description-text">
            {problem.description &&
              problem.description
                .split('\n')
                .map((line, idx) => (
                  <p key={idx}>
                    {line || '\u00A0'}
                  </p>
                ))}
          </div>

        </div>

        {/* Constraints */}

        {problem.constraints && (
          <div className="problem-constraints">

            <h2 className="description-heading">
              Constraints
            </h2>

            <div className="description-text">
              {problem.constraints
                .split('\n')
                .map((line, idx) => (
                  <p key={idx}>
                    {line || '\u00A0'}
                  </p>
                ))}
            </div>

          </div>
        )}

      </div>

      {/* =====================================================
          PROGRESS ACTIONS
         ===================================================== */}

      <div className="progress-actions-panel">

        <div className="progress-actions-header">

          <div>
            <h2>
              Problem Progress
            </h2>

            <p>
              Track your progress while solving this problem.
            </p>
          </div>

        </div>

        <div className="progress-actions-buttons">

          <button
            type="button"
            className="progress-attempt-btn"
            onClick={handleAttempted}
            disabled={isRunning || isSubmitting}
          >
            Mark Attempted
          </button>

          <button
            type="button"
            className="progress-solved-btn"
            onClick={handleSolved}
            disabled={isRunning || isSubmitting}
          >
            ✓ Mark as Solved
          </button>

        </div>

        {progressMessage && (
          <div className="alert alert-success">
            {progressMessage}
          </div>
        )}

        {progressError && (
          <div className="alert alert-error">
            {progressError}
          </div>
        )}

      </div>

      {/* =====================================================
          CODE EDITOR
         ===================================================== */}

      <div className="code-editor-panel">

        {/* Editor Header */}

        <div className="code-editor-header">

          <div className="code-editor-title-section">

            <span className="code-editor-kicker">
              SOLUTION
            </span>

            <h2>
              Code Editor
            </h2>

            <p>
              Write your solution and test it here.
            </p>

          </div>

          {/* Language Controls */}

          <div className="code-editor-controls">

            <select
              value={language}
              onChange={handleLanguageChange}
              className="editor-language-select"
              disabled={isRunning || isSubmitting}
            >
              <option value="java">
                Java
              </option>

              <option value="javascript">
                JavaScript
              </option>

              <option value="python">
                Python
              </option>

              <option value="cpp">
                C++
              </option>

            </select>

            <button
              type="button"
              className="editor-reset-btn"
              onClick={handleResetCode}
              disabled={isRunning || isSubmitting}
            >
              Reset
            </button>

          </div>

        </div>

        {/* =================================================
            LANGUAGE INDICATOR
           ================================================= */}

        <div className="editor-language-indicator">

          <span className="editor-language-dot"></span>

          <span>
            {languageNames[language]}
          </span>

          <span className="editor-language-separator">
            •
          </span>

          <span>
            Monaco Editor
          </span>

        </div>

        {/* =================================================
            MONACO EDITOR
           ================================================= */}

        <div className="monaco-editor-wrapper">

          <Editor
            height="500px"
            language={language}
            theme="vs-dark"
            value={currentCode}
            onChange={handleCodeChange}
            options={{
              minimap: {
                enabled: true,
              },

              fontSize: 14,

              lineNumbers: 'on',

              automaticLayout: true,

              tabSize: 4,

              insertSpaces: true,

              wordWrap: 'on',

              scrollBeyondLastLine: false,

              padding: {
                top: 16,
                bottom: 16,
              },

              suggestOnTriggerCharacters: true,

              quickSuggestions: true,

              formatOnPaste: true,

              formatOnType: true,

              cursorBlinking: 'smooth',

              smoothScrolling: true,

              renderWhitespace: 'selection',

              bracketPairColorization: {
                enabled: true,
              },

              guides: {
                bracketPairs: true,
                indentation: true,
              },
            }}
          />

        </div>

        {/* =================================================
            EDITOR ACTION BAR
           ================================================= */}

        <div className="editor-action-bar">

          <div className="editor-status">

            <span className="editor-status-dot"></span>

            <span>
              {isRunning
                ? 'Running code...'
                : isSubmitting
                  ? 'Submitting solution...'
                  : 'Ready'}
            </span>

          </div>

          <div className="editor-buttons">

            <button
              type="button"
              className="editor-run-btn"
              onClick={handleRunCode}
              disabled={isRunning || isSubmitting}
            >
              {isRunning
                ? 'Running...'
                : '▶ Run Code'}
            </button>

            <button
              type="button"
              className="editor-submit-btn"
              onClick={handleSubmitCode}
              disabled={isRunning || isSubmitting}
            >
              {isSubmitting
                ? 'Submitting...'
                : '✓ Submit Code'}
            </button>

          </div>

        </div>

        {/* =================================================
            OUTPUT / SUBMISSION RESULT
           ================================================= */}

        <div className="editor-output-panel">

          <div className="editor-output-header">

            <span>
              {submissionResult
                ? 'SUBMISSION RESULT'
                : 'OUTPUT'}
            </span>

            {(output || submissionResult) && (
              <button
                type="button"
                className="clear-output-btn"
                onClick={() => {
                  setOutput('');
                  setSubmissionResult(null);
                }}
              >
                Clear
              </button>
            )}

          </div>

          {/* =================================================
              SUBMISSION RESULT
             ================================================= */}

          {submissionResult ? (
            (() => {
              const config = getSubmissionConfig(
                submissionResult.status
              );

              const testCases =
                submissionResult.testCases || [];

              const passedCount =
                testCases.filter(
                  (testCase) =>
                    testCase.status === 'PASSED'
                ).length;

              const failedCount =
                testCases.filter(
                  (testCase) =>
                    testCase.status !== 'PASSED'
                ).length;

              return (
                <div
                  className={`submission-result-card submission-result-${config.className}`}
                >

                  {/* ==========================================
                      SUBMISSION SUMMARY
                     ========================================== */}

                  <div className="submission-result-main">

                    <div className="submission-result-icon">
                      {config.icon}
                    </div>

                    <div className="submission-result-info">

                      <div className="submission-result-title">
                        {config.title}
                      </div>

                      <div className="submission-result-subtitle">
                        {submissionResult.errorMessage ||
                          config.subtitle}
                      </div>

                    </div>

                  </div>

                  {/* ==========================================
                      SUBMISSION DETAILS
                     ========================================== */}

                  <div className="submission-result-details">

                    <div className="submission-detail-item">

                      <span className="submission-detail-label">
                        STATUS
                      </span>

                      <span className="submission-detail-value">
                        {submissionResult.status ||
                          'UNKNOWN'}
                      </span>

                    </div>

                    <div className="submission-detail-item">

                      <span className="submission-detail-label">
                        LANGUAGE
                      </span>

                      <span className="submission-detail-value">
                        {languageNames[language]}
                      </span>

                    </div>

                    {submissionResult.executionTimeMs !==
                      null &&
                      submissionResult.executionTimeMs !==
                        undefined && (
                        <div className="submission-detail-item">

                          <span className="submission-detail-label">
                            EXECUTION TIME
                          </span>

                          <span className="submission-detail-value">
                            {
                              submissionResult.executionTimeMs
                            }{' '}
                            ms
                          </span>

                        </div>
                      )}

                  </div>

                  {/* ==========================================
                      TEST CASE RESULTS
                     ========================================== */}

                  {testCases.length > 0 && (
                    <div className="test-case-results-section">

                      <div className="test-case-results-header">

                        <div>

                          <div className="test-case-results-title">
                            TEST CASES
                          </div>

                          <div className="test-case-results-summary">
                            {passedCount} / {testCases.length}{' '}
                            passed
                          </div>

                        </div>

                        {failedCount === 0 ? (
                          <span className="test-case-all-passed">
                            ✓ All Passed
                          </span>
                        ) : (
                          <span className="test-case-some-failed">
                            {failedCount} Failed
                          </span>
                        )}

                      </div>

                      <div className="test-case-results-list">

                        {testCases.map((testCase) => {

                          const isPassed =
                            testCase.status ===
                            'PASSED';

                          let statusLabel;

                          if (
                            testCase.status ===
                            'TIME_LIMIT_EXCEEDED'
                          ) {
                            statusLabel =
                              'Time Limit Exceeded';
                          } else if (
                            testCase.status ===
                            'COMPILATION_ERROR'
                          ) {
                            statusLabel =
                              'Compilation Error';
                          } else if (
                            testCase.status ===
                            'RUNTIME_ERROR'
                          ) {
                            statusLabel =
                              'Runtime Error';
                          } else if (isPassed) {
                            statusLabel = 'Passed';
                          } else {
                            statusLabel = 'Failed';
                          }

                          return (
                            <div
                              key={
                                testCase.testCaseId
                              }
                              className={`test-case-result-item ${
                                isPassed
                                  ? 'test-case-passed'
                                  : 'test-case-failed'
                              }`}
                            >

                              <div className="test-case-result-left">

                                <div className="test-case-status-icon">
                                  {isPassed
                                    ? '✓'
                                    : '×'}
                                </div>

                                <div className="test-case-result-info">

                                  <div className="test-case-result-name">
                                    Test Case{' '}
                                    {
                                      testCase.testCaseNumber
                                    }
                                  </div>

                                  <div className="test-case-result-status">
                                    {statusLabel}
                                  </div>

                                </div>

                              </div>

                              <div className="test-case-result-right">

                                {testCase.hidden && (
                                  <span className="test-case-hidden-badge">
                                    Hidden
                                  </span>
                                )}

                                {testCase.executionTimeMs !==
                                  null &&
                                  testCase.executionTimeMs !==
                                    undefined && (
                                    <span className="test-case-time">
                                      {
                                        testCase.executionTimeMs
                                      }{' '}
                                      ms
                                    </span>
                                  )}

                              </div>

                            </div>
                          );
                        })}

                      </div>

                    </div>
                  )}

                  {/* ==========================================
                      SUCCESS MESSAGE
                     ========================================== */}

                  {submissionResult.status ===
                    'ACCEPTED' && (
                    <div className="submission-success-message">
                      ✓ All test cases passed successfully.
                    </div>
                  )}

                </div>
              );
            })()
          ) : output ? (

            /* =================================================
               NORMAL RUN OUTPUT
               ================================================= */

            <div className="editor-output-content">
              <pre>
                {output}
              </pre>
            </div>

          ) : (

            /* =================================================
               EMPTY OUTPUT
               ================================================= */

            <div className="editor-output-content">
              <span className="output-placeholder">
                Run your code to see the output here.
              </span>
            </div>

          )}

        </div>

      </div>

    </div>
  );
}

export default ProblemDetailsPage;