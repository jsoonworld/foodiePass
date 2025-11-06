import { useState } from 'react';
import { submitSurvey } from '@/lib/api';

export function useSurvey() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const submit = async (scanId: string, hasConfidence: boolean) => {
    setLoading(true);
    setError(null);
    try {
      await submitSurvey({ scanId, hasConfidence });
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || 'Failed to submit survey';
      setError(errorMessage);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return { submit, loading, error };
}
