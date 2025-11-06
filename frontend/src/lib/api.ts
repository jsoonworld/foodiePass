import axios from 'axios';
import type {
  MenuScanRequest,
  MenuScanResponse,
  SurveyRequest,
  SurveyResponse,
  LanguageResponse,
  CurrencyResponse,
} from './types';

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

export async function scanMenu(request: MenuScanRequest): Promise<MenuScanResponse> {
  const response = await api.post<MenuScanResponse>('/api/menus/scan', request);
  return response.data;
}

export async function submitSurvey(request: SurveyRequest): Promise<SurveyResponse> {
  const response = await api.post<SurveyResponse>('/api/surveys', request);
  return response.data;
}

export async function getLanguages(): Promise<LanguageResponse[]> {
  const response = await api.get<LanguageResponse[]>('/api/language');
  return response.data;
}

export async function getCurrencies(): Promise<CurrencyResponse[]> {
  const response = await api.get<CurrencyResponse[]>('/api/currency');
  return response.data;
}
