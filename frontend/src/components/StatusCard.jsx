function StatusCard({ backendStatus, onRefresh }) {
  return (
    <div className="card">
      <h1 className="title">AI DSA Platform</h1>
      <div className="status-container">
        <div className="status-item">
          <span className="status-label">Frontend:</span>
          <span className="status-badge status-running">Running</span>
        </div>
        <div className="status-item">
          <span className="status-label">Backend:</span>
          {backendStatus === 'connecting' && (
            <span className="status-badge status-connecting">Connecting...</span>
          )}
          {backendStatus === 'connected' && (
            <span className="status-badge status-connected">Connected ✓</span>
          )}
          {backendStatus === 'disconnected' && (
            <span className="status-badge status-disconnected">Disconnected</span>
          )}
        </div>
      </div>
      <button className="retry-btn" onClick={onRefresh}>
        Refresh Status
      </button>
    </div>
  );
}

export default StatusCard;
