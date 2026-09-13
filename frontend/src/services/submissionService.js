import { apiClient } from './apiClient';

export const submitCode = async (problemId, language, code) => {
  return await apiClient('/api/submissions', {
    method: 'POST',
    body: JSON.stringify({
      problemId,
      language,
      code,
    }),
  });
};

export const getSubmissionHistory = async () => {
  return await apiClient('/api/submissions');
};

export const getSubmissionAnalytics = async () => {
  return await apiClient('/api/submissions/analytics');
};