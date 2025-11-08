import axios, { AxiosError } from 'axios';
import type {
  MenuScanRequest,
  MenuScanResponse,
  SurveyRequest,
  SurveyResponse,
  LanguageResponse,
  CurrencyResponse,
  GlobalResponse,
} from './types';

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 150000, // 150 seconds timeout (menu processing can take up to 2 minutes)
});

// Request interceptor for logging and adding request metadata
api.interceptors.request.use(
  (config) => {
    // Add request timestamp for performance monitoring
    config.metadata = { startTime: new Date().getTime() };

    if (import.meta.env.DEV) {
      console.log(`[API Request] ${config.method?.toUpperCase()} ${config.url}`);
    }

    return config;
  },
  (error) => {
    console.error('[API Request Error]', error);
    return Promise.reject(error);
  }
);

// Response interceptor for error handling and logging
api.interceptors.response.use(
  (response) => {
    // Calculate request duration
    const duration = new Date().getTime() - response.config.metadata.startTime;

    if (import.meta.env.DEV) {
      console.log(
        `[API Response] ${response.config.method?.toUpperCase()} ${response.config.url} - ${duration}ms`
      );
    }

    return response;
  },
  (error: AxiosError) => {
    // Enhanced error handling
    if (error.response) {
      // Server responded with error status
      const status = error.response.status;
      const message = (error.response.data as any)?.message || error.message;

      console.error(`[API Error ${status}]`, message);

      // Add user-friendly error messages
      if (status === 400) {
        error.message = '잘못된 요청입니다. 입력 내용을 확인해주세요.';
      } else if (status === 404) {
        error.message = '요청한 리소스를 찾을 수 없습니다.';
      } else if (status === 500) {
        error.message = '서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
      } else if (status >= 500) {
        error.message = '서버에 문제가 발생했습니다.';
      }
    } else if (error.request) {
      // Request was made but no response
      console.error('[API Error] No response received', error.request);
      error.message = '서버에 연결할 수 없습니다. 인터넷 연결을 확인해주세요.';
    } else {
      // Something else happened
      console.error('[API Error]', error.message);
    }

    return Promise.reject(error);
  }
);

// Extend AxiosRequestConfig to include metadata
declare module 'axios' {
  export interface AxiosRequestConfig {
    metadata?: {
      startTime: number;
    };
  }
}

export async function scanMenu(request: MenuScanRequest): Promise<MenuScanResponse> {
  const response = await api.post<MenuScanResponse>('/api/menus/scan', request);
  return response.data;
}

export async function submitSurvey(request: SurveyRequest): Promise<SurveyResponse> {
  const response = await api.post<SurveyResponse>('/api/surveys', request);
  return response.data;
}

export async function getLanguages(): Promise<LanguageResponse[]> {
  const response = await api.get<GlobalResponse<LanguageResponse[]>>('/api/language');
  return response.data.result;
}

export async function getCurrencies(): Promise<CurrencyResponse[]> {
  const response = await api.get<GlobalResponse<CurrencyResponse[]>>('/api/currency');
  return response.data.result;
}
