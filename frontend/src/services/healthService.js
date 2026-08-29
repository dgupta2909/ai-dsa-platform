import { apiClient } from './apiClient';

export async function getHealthStatus() {
  return apiClient('/api/health');
}
