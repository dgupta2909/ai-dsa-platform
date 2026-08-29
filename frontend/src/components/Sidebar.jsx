function Sidebar({ activeView, onNavigate, user, onLogout }) {
  return (
    <aside className="sidebar">
      <div className="sidebar-brand">
        <span className="brand-icon">⚡</span>
        <span className="brand-title">AI DSA Platform</span>
      </div>

      <nav className="sidebar-nav">
        <button
          type="button"
          className={`sidebar-link ${activeView === 'dashboard' ? 'active' : ''}`}
          onClick={() => onNavigate('dashboard')}
        >
          <span className="link-icon">📊</span>
          <span>Dashboard</span>
        </button>

        <button
          type="button"
          className={`sidebar-link ${activeView === 'problems' || activeView === 'problem-details' ? 'active' : ''}`}
          onClick={() => onNavigate('problems')}
        >
          <span className="link-icon">📚</span>
          <span>Problem Library</span>
        </button>

        <button
          type="button"
          className={`sidebar-link ${activeView === 'status' ? 'active' : ''}`}
          onClick={() => onNavigate('status')}
        >
          <span className="link-icon">🩺</span>
          <span>System Health</span>
        </button>
      </nav>

      <div className="sidebar-footer">
        <div className="user-profile-badge">
          <div className="user-avatar">{user.fullName ? user.fullName.charAt(0).toUpperCase() : 'U'}</div>
          <div className="user-details">
            <span className="user-name">{user.fullName}</span>
            <span className="user-email">{user.email}</span>
          </div>
        </div>
        <button type="button" className="sidebar-logout-btn" onClick={onLogout}>
          Sign Out
        </button>
      </div>
    </aside>
  );
}

export default Sidebar;
