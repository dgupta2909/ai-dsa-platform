import { apiClient } from './apiClient';

export const runCode = async (language, code) => {
  return await apiClient('/api/code/run', {
    method: 'POST',
    body: JSON.stringify({
      language,
      code,
    }),
  });
};