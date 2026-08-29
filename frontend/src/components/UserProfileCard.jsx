import { useState } from 'react';
import { getCurrentUser } from '../services/authService';

function UserProfileCard({ user, onLogout }) {
  const [meResult, setMeResult] = useState(null);
  const [verifying, setVerifying] = useState(false);
  const [error, setError] = useState(null);

  const handleVerifyMe = async () => {
    setVerifying(true);
    setError(null);
    setMeResult(null);
    try {
      const res = await getCurrentUser();
      setMeResult(res);
    } catch (err) {
      setError(err.message || 'Protected endpoint verification failed');
    } finally {
      setVerifying(false);
    }
  };

  return (
    <div className="card">
      <h2 className="title">Protected Session</h2>
      <div className="alert alert-success">
        Authenticated ✓ Welcome back, <strong>{user.fullName}</strong>!
      </div>
      <div className="status-container">
        <div className="status-item">
          <span className="status-label">User ID:</span>
          <span className="status-value">#{user.id}</span>
        </div>
        <div className="status-item">
          <span className="status-label">Full Name:</span>
          <span className="status-value">{user.fullName}</span>
        </div>
        <div className="status-item">
          <span className="status-label">Email:</span>
          <span className="status-value">{user.email}</span>
        </div>
        <div className="status-item">
          <span className="status-label">JWT Authentication:</span>
          <span className="status-badge status-connected">Active Token ✓</span>
        </div>
      </div>

      <div className="verify-box">
        <button
          type="button"
          className="secondary-btn"
          onClick={handleVerifyMe}
          disabled={verifying}
        >
          {verifying ? 'Verifying...' : 'Verify GET /api/auth/me'}
        </button>
        {meResult && (
          <div className="alert alert-success mt-2">
            Endpoint <code>/api/auth/me</code> verified! Subject: {meResult.email}
          </div>
        )}
        {error && (
          <div className="alert alert-error mt-2">
            {error}
          </div>
        )}
      </div>

      <button className="logout-btn" onClick={onLogout}>
        Sign Out
      </button>
    </div>
  );
}

export default UserProfileCard;
