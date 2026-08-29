import { apiClient } from './apiClient';

export async function getProblems(params = {}) {
  const query = new URLSearchParams();
  if (params.difficulty) query.append('difficulty', params.difficulty);
  if (params.category) query.append('category', params.category);
  if (params.search) query.append('search', params.search);

  const queryString = query.toString();
  const endpoint = queryString ? `/api/problems?${queryString}` : '/api/problems';
  return apiClient(endpoint);
}

export async function getProblemById(id) {
  return apiClient(`/api/problems/${id}`);
}

export async function getProblemBySlug(slug) {
  return apiClient(`/api/problems/slug/${slug}`);
}
