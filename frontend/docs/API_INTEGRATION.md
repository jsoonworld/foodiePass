# FoodiePass Frontend - API Integration

> **목적**: 백엔드 API 연동 방법 및 타입 정의

---

## Base URL

```typescript
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
```

---

## API Endpoints

### 1. POST /api/menus/scan

**Request**:
```typescript
interface MenuScanRequest {
  image: string;              // Base64 encoded
  sourceLanguage?: string;    // Optional, default: "auto"
  targetLanguage: string;     // Required (예: "ko", "en")
  sourceCurrency?: string;    // Optional
  targetCurrency: string;     // Required (예: "KRW", "USD")
}
```

**Response**:
```typescript
interface MenuScanResponse {
  scanId: string;                    // UUID
  abGroup: 'CONTROL' | 'TREATMENT';  // A/B 그룹
  items: MenuItem[];
  processingTime: number;            // 초
}

interface MenuItem {
  id: string;
  originalName: string;
  translatedName: string;
  description?: string;              // Treatment만
  imageUrl?: string;                 // Treatment만
  priceInfo: PriceInfo;
  matchConfidence?: number;          // Treatment만
}

interface PriceInfo {
  originalAmount: number;
  originalCurrency: string;
  originalFormatted: string;
  convertedAmount: number;
  convertedCurrency: string;
  convertedFormatted: string;
}
```

---

### 2. POST /api/surveys

**Request**:
```typescript
interface SurveyRequest {
  scanId: string;            // UUID
  hasConfidence: boolean;    // Yes=true, No=false
}
```

**Response**:
```typescript
interface SurveyResponse {
  success: boolean;
  message: string;
}
```

---

## API Client Implementation

### Axios Setup

```typescript
// lib/api.ts
import axios from 'axios';

export const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080',
  withCredentials: true,  // 세션 쿠키 포함
  headers: {
    'Content-Type': 'application/json',
  },
});

// API 함수들
export async function scanMenu(request: MenuScanRequest): Promise<MenuScanResponse> {
  const response = await api.post<MenuScanResponse>('/api/menus/scan', request);
  return response.data;
}

export async function submitSurvey(scanId: string, hasConfidence: boolean): Promise<void> {
  await api.post('/api/surveys', { scanId, hasConfidence });
}
```

---

## React Hook 예시

```typescript
// hooks/useMenuScan.ts
import { useState } from 'react';
import { scanMenu, MenuScanRequest, MenuScanResponse } from '@/lib/api';

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
      setError(err.response?.data?.message || 'Scan failed');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return { scan, loading, error, result };
}
```

---

## Error Handling

```typescript
// utils/api-error.ts
export interface ApiError {
  error: string;
  message: string;
  timestamp: string;
}

export function handleApiError(error: any): string {
  if (error.response) {
    const apiError = error.response.data as ApiError;
    return apiError.message || 'Request failed';
  }
  return 'Network error';
}
```

---

## 참고 문서

Backend API Spec: [../backend/docs/API_SPEC.md](../../backend/docs/API_SPEC.md)
