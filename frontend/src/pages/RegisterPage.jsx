import RegisterForm from '../components/RegisterForm';

function RegisterPage({ onRegisterSuccess, onSwitchToLogin }) {
  return (
    <div className="page-container">
      <RegisterForm
        onRegisterSuccess={onRegisterSuccess}
        onSwitchToLogin={onSwitchToLogin}
      />
    </div>
  );
}

export default RegisterPage;
