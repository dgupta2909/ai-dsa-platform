import StatusCard from '../components/StatusCard';

function StatusPage({ backendStatus, onRefresh }) {
  return (
    <div className="page-container">
      <StatusCard backendStatus={backendStatus} onRefresh={onRefresh} />
    </div>
  );
}

export default StatusPage;
