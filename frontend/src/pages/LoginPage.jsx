import LoginForm from '../components/LoginForm';

function LoginPage({ onLoginSuccess, onSwitchToRegister }) {
  return (
    <div className="page-container">
      <LoginForm
        onLoginSuccess={onLoginSuccess}
        onSwitchToRegister={onSwitchToRegister}
      />
    </div>
  );
}

export default LoginPage;
