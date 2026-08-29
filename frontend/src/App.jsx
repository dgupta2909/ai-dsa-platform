import { useState, useEffect } from 'react';
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

  // Unauthenticated tab: 'login' | 'register' | 'status'
  const [authTab, setAuthTab] = useState('login');
  const [successNotice, setSuccessNotice] = useState(null);

  // Authenticated view: 'dashboard' | 'problems' | 'problem-details' | 'status'
  const [activeView, setActiveView] = useState('dashboard');
  const [selectedProblemId, setSelectedProblemId] = useState(null);

  // Check stored JWT on initial mount
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
    setActiveView('dashboard');
    setSuccessNotice(null);
  };

  const handleRegisterSuccess = (user) => {
    setSuccessNotice(`Registration successful for ${user.email}! Please sign in.`);
    setAuthTab('login');
  };

  const handleLogout = () => {
    localStorage.removeItem('auth_token');
    setCurrentUser(null);
    setSuccessNotice(null);
    setAuthTab('login');
    setActiveView('dashboard');
    setSelectedProblemId(null);
  };

  const handleNavigate = (view) => {
    setActiveView(view);
    if (view !== 'problem-details') {
      setSelectedProblemId(null);
    }
  };

  const handleSelectProblem = (id) => {
    setSelectedProblemId(id);
    setActiveView('problem-details');
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

  // Authenticated Layout with Sidebar and Dashboard/Problem Library Views
  if (currentUser) {
    return (
      <div className="app-layout">
        <Sidebar
          activeView={activeView}
          onNavigate={handleNavigate}
          user={currentUser}
          onLogout={handleLogout}
        />
        <main className="main-viewport">
          {activeView === 'dashboard' && (
            <DashboardPage
              user={currentUser}
              onNavigateToProblems={() => handleNavigate('problems')}
              onSelectProblem={handleSelectProblem}
            />
          )}

          {activeView === 'problems' && (
            <ProblemLibraryPage
              onSelectProblem={handleSelectProblem}
            />
          )}

          {activeView === 'problem-details' && (
            <ProblemDetailsPage
              problemId={selectedProblemId}
              onBackToProblems={() => handleNavigate('problems')}
            />
          )}

          {activeView === 'status' && (
            <div className="main-content-view">
              <StatusPage backendStatus={status} onRefresh={checkHealth} />
            </div>
          )}
        </main>
      </div>
    );
  }

  // Unauthenticated Layout
  return (
    <div className="container">
      <div className="app-wrapper">
        <header className="app-header">
          <h1 className="main-logo">AI DSA Platform</h1>
          <nav className="tab-nav">
            <button
              className={`nav-btn ${authTab === 'login' ? 'active' : ''}`}
              onClick={() => { setAuthTab('login'); setSuccessNotice(null); }}
            >
              Sign In
            </button>
            <button
              className={`nav-btn ${authTab === 'register' ? 'active' : ''}`}
              onClick={() => { setAuthTab('register'); setSuccessNotice(null); }}
            >
              Register
            </button>
            <button
              className={`nav-btn ${authTab === 'status' ? 'active' : ''}`}
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
              onLoginSuccess={handleLoginSuccess}
              onSwitchToRegister={() => { setAuthTab('register'); setSuccessNotice(null); }}
            />
          )}

          {authTab === 'register' && (
            <RegisterPage
              onRegisterSuccess={handleRegisterSuccess}
              onSwitchToLogin={() => { setAuthTab('login'); setSuccessNotice(null); }}
            />
          )}

          {authTab === 'status' && (
            <StatusPage backendStatus={status} onRefresh={checkHealth} />
          )}
        </main>
      </div>
    </div>
  );
}

export default App;
