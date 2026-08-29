import { apiClient } from './apiClient';

export async function registerUser({ fullName, email, password }) {
  return apiClient('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify({ fullName, email, password }),
  });
}

export async function loginUser({ email, password }) {
  return apiClient('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  });
}

export async function getCurrentUser() {
  return apiClient('/api/auth/me', {
    method: 'GET',
  });
}
