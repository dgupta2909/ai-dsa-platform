import { apiClient } from './apiClient';

export const getRecommendations = async () => {
  return await apiClient('/api/recommendations');
};