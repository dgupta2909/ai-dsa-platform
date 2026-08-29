const API_BASE_URL = 'http://localhost:8081/api';

const getAuthHeaders = () => {
  const token = localStorage.getItem('auth_token');

  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };
};

export const getProblemProgress = async (problemId) => {
  const response = await fetch(
    `${API_BASE_URL}/progress/${problemId}`,
    {
      method: 'GET',
      headers: getAuthHeaders(),
    }
  );

  if (response.status === 204) {
    return null;
  }

  if (!response.ok) {
    throw new Error('Failed to load problem progress');
  }

  return response.json();
};

export const markProblemAttempted = async (problemId) => {
  const response = await fetch(
    `${API_BASE_URL}/progress/${problemId}/attempt`,
    {
      method: 'POST',
      headers: getAuthHeaders(),
    }
  );

  if (!response.ok) {
    throw new Error('Failed to mark problem as attempted');
  }

  return response.json();
};

export const markProblemSolved = async (problemId) => {
  const response = await fetch(
    `${API_BASE_URL}/progress/${problemId}/solve`,
    {
      method: 'POST',
      headers: getAuthHeaders(),
    }
  );

  if (!response.ok) {
    throw new Error('Failed to mark problem as solved');
  }

  return response.json();
};

export const getUserProgress = async () => {
  const response = await fetch(
    `${API_BASE_URL}/progress`,
    {
      method: 'GET',
      headers: getAuthHeaders(),
    }
  );

  if (!response.ok) {
    throw new Error('Failed to load user progress');
  }

  return response.json();
};

export const getSolvedCount = async () => {
  const response = await fetch(
    `${API_BASE_URL}/progress/solved-count`,
    {
      method: 'GET',
      headers: getAuthHeaders(),
    }
  );

  if (!response.ok) {
    throw new Error('Failed to load solved count');
  }

  return response.json();
};