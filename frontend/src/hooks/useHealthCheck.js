import { useState, useEffect, useCallback } from 'react';
import { getHealthStatus } from '../services/healthService';

export function useHealthCheck() {
  const [status, setStatus] = useState('connecting'); // 'connecting' | 'connected' | 'disconnected'

  const checkHealth = useCallback(async () => {
    setStatus('connecting');
    try {
      const data = await getHealthStatus();
      if (data && data.status === 'ok') {
        setStatus('connected');
      } else {
        setStatus('disconnected');
      }
    } catch (err) {
      console.error('Failed to reach backend:', err);
      setStatus('disconnected');
    }
  }, []);

  useEffect(() => {
    checkHealth();
  }, [checkHealth]);

  return { status, checkHealth };
}
