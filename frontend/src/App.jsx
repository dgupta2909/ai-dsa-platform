import { useState, useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate, useNavigate, useLocation, useParams } from 'react-router-dom';

import Sidebar from './components/Sidebar';
import DashboardPage from './pages/DashboardPage';
import ProblemLibraryPage from './pages/ProblemLibraryPage';
import ProblemDetailsPage from './pages/ProblemDetailsPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import StatusPage from './pages/StatusPage';

import { getCurrentUser } from './services/authService';
import { useHealthCheck } from './hooks/useHealthCheck';

import './App.css';

function App() {
  const { status, checkHealth } = useHealthCheck();

  const [currentUser, setCurrentUser] = useState(null);
  const [isLoadingAuth, setIsLoadingAuth] = useState(true);

  const [authTab, setAuthTab] = useState('login');
  const [successNotice, setSuccessNotice] = useState(null);

  useEffect(() => {
    const token = localStorage.getItem('auth_token');

    if (token) {
      getCurrentUser()
        .then((userData) => {
          setCurrentUser(userData);
        })
        .catch(() => {
          localStorage.removeItem('auth_token');
          setCurrentUser(null);
        })
        .finally(() => {
          setIsLoadingAuth(false);
        });
    } else {
      setIsLoadingAuth(false);
    }
  }, []);

  const handleLoginSuccess = (response) => {
    if (response.token) {
      localStorage.setItem('auth_token', response.token);
    }

    setCurrentUser(response);
    setSuccessNotice(null);
  };

  const handleRegisterSuccess = (user) => {
    setSuccessNotice(
      `Registration successful for ${user.email}! Please sign in.`
    );
    setAuthTab('login');
  };

  const handleLogout = () => {
    localStorage.removeItem('auth_token');
    setCurrentUser(null);
    setSuccessNotice(null);
    setAuthTab('login');
  };

  if (isLoadingAuth) {
    return (
      <div className="container">
        <div className="card">
          <h2 className="title">Loading Session...</h2>
        </div>
      </div>
    );
  }

  return (
    <BrowserRouter>
      {currentUser ? (
        <AuthenticatedApp
          currentUser={currentUser}
          status={status}
          checkHealth={checkHealth}
          onLogout={handleLogout}
        />
      ) : (
        <UnauthenticatedApp
          authTab={authTab}
          setAuthTab={setAuthTab}
          successNotice={successNotice}
          setSuccessNotice={setSuccessNotice}
          onLoginSuccess={handleLoginSuccess}
          onRegisterSuccess={handleRegisterSuccess}
          status={status}
          checkHealth={checkHealth}
        />
      )}
    </BrowserRouter>
  );
}


/* =========================================================
   AUTHENTICATED APP
   ========================================================= */

function AuthenticatedApp({
  currentUser,
  status,
  checkHealth,
  onLogout,
}) {
  const navigate = useNavigate();
  const location = useLocation();

  const getActiveView = () => {
    if (location.pathname.startsWith('/problems/')) {
      return 'problem-details';
    }

    if (location.pathname === '/problems') {
      return 'problems';
    }

    if (location.pathname === '/status') {
      return 'status';
    }

    return 'dashboard';
  };

  const activeView = getActiveView();

  const handleNavigate = (view) => {
    if (view === 'dashboard') {
      navigate('/dashboard');
    } else if (view === 'problems') {
      navigate('/problems');
    } else if (view === 'status') {
      navigate('/status');
    }
  };

  const handleSelectProblem = (id) => {
    navigate(`/problems/${id}`);
  };

  return (
    <div className="app-layout">
      <Sidebar
        activeView={activeView}
        onNavigate={handleNavigate}
        user={currentUser}
        onLogout={onLogout}
      />

      <main className="main-viewport">
        <Routes>
          <Route
            path="/"
            element={<Navigate to="/dashboard" replace />}
          />

          <Route
            path="/dashboard"
            element={
              <DashboardPage
                user={currentUser}
                onNavigateToProblems={() => navigate('/problems')}
                onSelectProblem={handleSelectProblem}
              />
            }
          />

          <Route
            path="/problems"
            element={
              <ProblemLibraryPage
                onSelectProblem={handleSelectProblem}
              />
            }
          />

          <Route
            path="/problems/:id"
            element={
              <ProblemDetailsRoute
                onBackToProblems={() => navigate('/problems')}
              />
            }
          />

          <Route
            path="/status"
            element={
              <div className="main-content-view">
                <StatusPage
                  backendStatus={status}
                  onRefresh={checkHealth}
                />
              </div>
            }
          />

          <Route
            path="*"
            element={<Navigate to="/dashboard" replace />}
          />
        </Routes>
      </main>
    </div>
  );
}


/* =========================================================
   PROBLEM DETAILS ROUTE
   ========================================================= */

function ProblemDetailsRoute({ onBackToProblems }) {
  const { id } = useParams();

  return (
    <ProblemDetailsPage
      problemId={id}
      onBackToProblems={onBackToProblems}
    />
  );
}


/* =========================================================
   UNAUTHENTICATED APP
   ========================================================= */

function UnauthenticatedApp({
  authTab,
  setAuthTab,
  successNotice,
  setSuccessNotice,
  onLoginSuccess,
  onRegisterSuccess,
  status,
  checkHealth,
}) {
  return (
    <div className="container">
      <div className="app-wrapper">
        <header className="app-header">
          <h1 className="main-logo">AI DSA Platform</h1>

          <nav className="tab-nav">
            <button
              className={`nav-btn ${
                authTab === 'login' ? 'active' : ''
              }`}
              onClick={() => {
                setAuthTab('login');
                setSuccessNotice(null);
              }}
            >
              Sign In
            </button>

            <button
              className={`nav-btn ${
                authTab === 'register' ? 'active' : ''
              }`}
              onClick={() => {
                setAuthTab('register');
                setSuccessNotice(null);
              }}
            >
              Register
            </button>

            <button
              className={`nav-btn ${
                authTab === 'status' ? 'active' : ''
              }`}
              onClick={() => setAuthTab('status')}
            >
              System Health
            </button>
          </nav>
        </header>

        {successNotice && (
          <div className="alert alert-success global-notice">
            {successNotice}
          </div>
        )}

        <main className="content-area">
          {authTab === 'login' && (
            <LoginPage
              onLoginSuccess={onLoginSuccess}
              onSwitchToRegister={() => {
                setAuthTab('register');
                setSuccessNotice(null);
              }}
            />
          )}

          {authTab === 'register' && (
            <RegisterPage
              onRegisterSuccess={onRegisterSuccess}
              onSwitchToLogin={() => {
                setAuthTab('login');
                setSuccessNotice(null);
              }}
            />
          )}

          {authTab === 'status' && (
            <StatusPage
              backendStatus={status}
              onRefresh={checkHealth}
            />
          )}
        </main>
      </div>
    </div>
  );
}

export default App;
