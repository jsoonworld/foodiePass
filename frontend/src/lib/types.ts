// Request Types
export interface MenuScanRequest {
  base64EncodedImage: string;
  originLanguageName?: string;
  userLanguageName: string;
  originCurrencyName?: string;
  userCurrencyName: string;
}

export interface SurveyRequest {
  scanId: string;
  hasConfidence: boolean;
}

// Response Types
export interface MenuScanResponse {
  scanId: string;
  abGroup: 'CONTROL' | 'TREATMENT';
  items: MenuItem[];
  processingTime: number;
}

export interface MenuItem {
  id: string;
  originalName: string;
  translatedName: string;
  description?: string;
  imageUrl?: string;
  priceInfo: PriceInfo;
  matchConfidence?: number;
}

export interface PriceInfo {
  originalAmount: number;
  originalCurrency: string;
  originalFormatted: string;
  convertedAmount: number;
  convertedCurrency: string;
  convertedFormatted: string;
}

export interface SurveyResponse {
  success: boolean;
  message: string;
}

export interface LanguageResponse {
  languageName: string;
}

export interface CurrencyResponse {
  currencyName: string;
  currencyCode?: string;
}

export interface ApiError {
  error: string;
  message: string;
  timestamp: string;
}

export interface GlobalResponse<T> {
  status: number;
  message: string;
  result: T;
}
