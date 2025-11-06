import { useState } from 'react';
import { scanMenu } from '@/lib/api';
import type { MenuScanRequest, MenuScanResponse } from '@/lib/types';

export function useMenuScan() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<MenuScanResponse | null>(null);

  const scan = async (request: MenuScanRequest) => {
    setLoading(true);
    setError(null);
    try {
      const response = await scanMenu(request);
      setResult(response);
      return response;
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || 'Failed to scan menu';
      setError(errorMessage);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return { scan, loading, error, result };
}
