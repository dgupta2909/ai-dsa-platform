import { useEffect, useState } from 'react';
import Editor from '@monaco-editor/react';
import { getProblemById } from '../services/problemService';
import {
  markProblemAttempted,
  markProblemSolved,
} from '../services/progressService';
import { runCode } from '../services/codeService';
/* =========================================================
   STARTER CODE FOR EACH LANGUAGE
   ========================================================= */

const starterCode = {
  java: `import java.util.*;

public class Solution {
    public static void main(String[] args) {

        // Write your solution here

    }
}`,

  javascript: `function solution() {

    // Write your solution here

}

console.log(solution());`,

  python: `def solution():

    # Write your solution here
    pass


if __name__ == "__main__":
    solution()`,

  cpp: `#include <bits/stdc++.h>
using namespace std;

int main() {

    // Write your solution here

    return 0;
}`
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
     
     Each language has its own code.
     This prevents losing the code when switching languages.
     ======================================================= */

  const [codes, setCodes] = useState({
    java: starterCode.java,
    javascript: starterCode.javascript,
    python: starterCode.python,
    cpp: starterCode.cpp,
  });

  const [output, setOutput] = useState('');
  const [isRunning, setIsRunning] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [progressMessage, setProgressMessage] = useState('');
  const [progressError, setProgressError] = useState('');

  /* =======================================================
     LOAD PROBLEM
     ======================================================= */

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

    // Clear previous output because the language changed.
    setOutput('');

    // Clear progress messages.
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

    if (!confirmed) return;

    setCodes((previousCodes) => ({
      ...previousCodes,
      [language]: starterCode[language],
    }));

    setOutput('');
  };

  /* =======================================================
     MARK ATTEMPTED
     ======================================================= */

  const handleAttempted = async () => {
    try {
      setProgressError('');
      setProgressMessage('');

      await markProblemAttempted(problemId);

      setProgressMessage('Problem marked as attempted.');
    } catch (err) {
      setProgressError(
        err.message || 'Failed to mark problem as attempted'
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

      setProgressMessage('Problem marked as solved!');
    } catch (err) {
      setProgressError(
        err.message || 'Failed to mark problem as solved'
      );
    }
  };

  /* =======================================================
     RUN CODE
     ======================================================= */

   const handleRunCode = async () => {
    if (!currentCode || !currentCode.trim()) {
      setOutput('Code cannot be empty.');
      return;
    }

    setIsRunning(true);
    setOutput('');

    try {
      const response = await runCode(language, currentCode);

      if (response.success) {
        setOutput(response.output || 'Program executed successfully.');
      } else {
        const errorMessage =
          response.error || 'Code execution failed.';

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

  const handleSubmitCode = () => {
    setIsSubmitting(true);
    setOutput('');

    setTimeout(() => {
      setOutput(
        `Submission received successfully.\n\nLanguage: ${languageNames[language]}\n\nThe online judge system will be connected in the next phase.`
      );

      setIsSubmitting(false);
    }, 600);
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
            className={`difficulty-badge badge-${problem.difficulty.toLowerCase()}`}
          >
            {problem.difficulty}
          </span>

          <span className="category-tag">
            {problem.category}
          </span>

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

        <div className="tags-container">

          {problem.tags &&
            problem.tags.map((tag, idx) => (
              <span
                key={idx}
                className="tag-pill"
              >
                {tag}
              </span>
            ))}

        </div>

        {/* Problem Description */}

        <div className="problem-description">

          <h2 className="description-heading">
            Problem Description
          </h2>

          <div className="description-text">

            {problem.description &&
              problem.description.split('\n').map((line, idx) => (
                <p key={idx}>
                  {line || '\u00A0'}
                </p>
              ))}

          </div>

        </div>

        {/* Constraints */}

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
          >
            Mark Attempted
          </button>

          <button
            type="button"
            className="progress-solved-btn"
            onClick={handleSolved}
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

              tabSize: language === 'python' ? 4 : 4,

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

            Ready

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
            OUTPUT
           ================================================= */}

        <div className="editor-output-panel">

          <div className="editor-output-header">

            <span>
              OUTPUT
            </span>

            {output && (
              <button
                type="button"
                className="clear-output-btn"
                onClick={() => setOutput('')}
              >
                Clear
              </button>
            )}

          </div>

          <div className="editor-output-content">

            {output ? (
              <pre>
                {output}
              </pre>
            ) : (
              <span className="output-placeholder">
                Run your code to see the output here.
              </span>
            )}

          </div>

        </div>

      </div>

    </div>
  );
}

export default ProblemDetailsPage;