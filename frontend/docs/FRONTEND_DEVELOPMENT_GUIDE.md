# FoodiePass MVP - Frontend Development Instructions

> **전달 대상**: 프론트엔드 개발 AI
> **목적**: 백엔드 API가 명확히 정의되어 있으므로, 지금 바로 프론트엔드를 개발할 수 있습니다.

---

## 프로젝트 개요

**FoodiePass**는 여행객이 외국어 메뉴판을 사진 찍으면 자동으로 번역하고, 음식 사진/설명/환율 정보를 제공하는 웹 애플리케이션입니다.

### 핵심 목표
이 MVP는 **A/B 테스트를 통한 가설 검증**이 목적입니다:

**H1 (핵심 가치 가설)**:
> "텍스트 번역만으로는 불안하지만, [사진 + 설명 + 환율]이 포함된 시각적 메뉴는 주문 확신을 준다."

**H3 (사용자 행동 가설)**:
> "시각적 메뉴 사용자는 텍스트만 사용자보다 확신도가 2배 이상 높다."

### MVP 범위
✅ **구현 필요**:
- 메뉴판 이미지 업로드 UI
- 언어/화폐 선택 UI
- Control UI (텍스트 + 환율만)
- Treatment UI (사진 + 설명 + 텍스트 + 환율)
- 확신도 설문 UI
- A/B 그룹에 따른 조건부 렌더링

❌ **제외**:
- 로그인/회원 시스템
- 장바구니
- 메뉴 저장/히스토리
- 결제 시스템

---

## 기술 스택 (추천)

```yaml
프레임워크: React 18+ (with Vite) 또는 Next.js 14+
언어: TypeScript
스타일링: Tailwind CSS
상태관리: React Query (API 캐싱) + React Context (UI 상태)
라우팅: React Router (Vite) 또는 App Router (Next.js)
HTTP Client: Axios
폼: React Hook Form (옵션)
이미지: react-dropzone (업로드 UI)
```

### 프로젝트 구조
```
frontend/
├── src/
│   ├── components/
│   │   ├── MenuUploader.tsx        # 이미지 업로드 UI
│   │   ├── LanguageSelector.tsx    # 언어 선택
│   │   ├── CurrencySelector.tsx    # 화폐 선택
│   │   ├── ControlMenu.tsx         # Control 그룹 UI
│   │   ├── TreatmentMenu.tsx       # Treatment 그룹 UI
│   │   ├── SurveyModal.tsx         # 설문 모달
│   │   ├── LoadingSpinner.tsx      # 로딩
│   │   └── ErrorMessage.tsx        # 에러
│   ├── pages/
│   │   ├── HomePage.tsx            # 업로드 페이지 (/)
│   │   └── MenuResultPage.tsx      # 메뉴 결과 (/menu/:scanId)
│   ├── hooks/
│   │   ├── useMenuScan.ts          # 메뉴 스캔 훅
│   │   └── useSurvey.ts            # 설문 제출 훅
│   ├── lib/
│   │   ├── api.ts                  # API 클라이언트
│   │   └── types.ts                # TypeScript 타입
│   ├── utils/
│   │   ├── imageUtils.ts           # Base64 변환
│   │   └── errorHandler.ts         # 에러 처리
│   └── App.tsx
├── package.json
└── tsconfig.json
```

---

## API 스펙 (백엔드 제공)

### Base URL
```
로컬: http://localhost:8080
프로덕션: https://api.foodiepass.com (TBD)
```

### 1. POST /api/menus/scan
**목적**: 메뉴판 업로드 → A/B 그룹 배정 → 파이프라인 실행

**Request**:
```typescript
interface MenuScanRequest {
  base64EncodedImage: string;      // Required: Base64 인코딩된 이미지
  originLanguageName?: string;     // Optional: 원본 언어 (기본값: "auto")
  userLanguageName: string;        // Required: 사용자 언어 (예: "Korean", "English")
  originCurrencyName?: string;     // Optional: 원본 화폐 (auto-detect)
  userCurrencyName: string;        // Required: 사용자 화폐 (예: "KRW Won", "USD Dollar")
}
```

**Response (Treatment 그룹)**:
```typescript
interface MenuScanResponse {
  scanId: string;                           // UUID
  abGroup: 'CONTROL' | 'TREATMENT';         // A/B 그룹
  items: MenuItem[];
  processingTime: number;                   // 초
}

interface MenuItem {
  id: string;
  originalName: string;                     // 원어 메뉴명
  translatedName: string;                   // 번역된 메뉴명
  description?: string;                     // Treatment만 포함
  imageUrl?: string;                        // Treatment만 포함
  priceInfo: {
    originalAmount: number;
    originalCurrency: string;
    originalFormatted: string;              // 예: "$15.00"
    convertedAmount: number;
    convertedCurrency: string;
    convertedFormatted: string;             // 예: "₩20,000"
  };
  matchConfidence?: number;                 // Treatment만 포함 (0-1)
}
```

**Response (Control 그룹)**:
- `description`, `imageUrl`, `matchConfidence` 필드 **없음**
- 오직 `originalName`, `translatedName`, `priceInfo`만 포함

---

### 2. POST /api/surveys
**목적**: 확신도 설문 응답 수집

**Request**:
```typescript
interface SurveyRequest {
  scanId: string;           // MenuScanResponse에서 받은 scanId
  hasConfidence: boolean;   // Yes=true, No=false
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

### 3. GET /api/language
**목적**: 지원하는 언어 목록 조회

**Response**:
```typescript
interface LanguageResponse {
  languageName: string;  // 예: "Korean", "English", "Japanese"
}

// 응답: LanguageResponse[]
// 총 133개 언어 지원
```

**주요 언어 예시**:
- Korean
- English
- Japanese
- Chinese (Simplified)
- Chinese (Traditional)
- Spanish
- French
- German
- Italian
- Thai
- Vietnamese

---

### 4. GET /api/currency
**목적**: 지원하는 화폐 목록 조회

**Response**:
```typescript
interface CurrencyResponse {
  currencyName: string;  // 예: "South Korean won"
  currencyCode: string;  // 예: "KRW"
}

// 응답: CurrencyResponse[]
// 총 145개 화폐 지원
```

**주요 화폐 예시**:
- South Korean won (KRW)
- United States Dollar (USD)
- Japanese Yen (JPY)
- Euro (EUR)
- Chinese Yuan (CNY)
- Pound sterling (GBP)
- Thai Baht (THB)
- Vietnamese dong (VND)

---

### 5. Error Responses
모든 에러는 다음 포맷:
```typescript
interface ApiError {
  error: string;          // 예: "BAD_REQUEST"
  message: string;        // 예: "Invalid image format"
  timestamp: string;
}
```

**주요 에러 코드**:
- `400 BAD_REQUEST`: 잘못된 요청 (이미지 포맷 오류 등)
- `413 PAYLOAD_TOO_LARGE`: 이미지 크기 초과 (10MB 제한)
- `500 INTERNAL_SERVER_ERROR`: 서버 오류
- `503 SERVICE_UNAVAILABLE`: 외부 API 연동 실패

---

## 사용자 플로우

```
[홈 페이지] (/)
  ↓
  ① 이미지 업로드 (드래그앤드롭 or 파일 선택)
  ② 언어 선택 (한국어, 영어, 일본어 등)
  ③ 화폐 선택 (KRW, USD, JPY 등)
  ④ "Scan Menu" 버튼 클릭
  ↓
[로딩 화면] (5초 이내)
  ↓
[메뉴 결과 페이지] (/menu/:scanId)
  ├─ abGroup === "CONTROL" → Control UI (텍스트만)
  └─ abGroup === "TREATMENT" → Treatment UI (사진 + 설명)
  ↓
[설문 모달] (5초 후 자동 표시)
  ↓
  "이 정보만으로 확신을 갖고 주문할 수 있습니까?"
  [Yes] [No]
  ↓
[완료] ("감사합니다!" 메시지)
```

---

## UI/UX 요구사항

### 1. Control UI (텍스트 전용)
**레이아웃**:
```
┌──────────────────────┐
│ Margherita Pizza     │ ← 원어 (16px, bold)
│ 마르게리타 피자        │ ← 번역 (14px, regular)
│ $15.00 (₩20,000)    │ ← 가격 (14px, semibold)
└──────────────────────┘
```

**제약사항**:
- ❌ 사진 없음
- ❌ 설명 없음
- ✅ 메뉴명 + 가격만

---

### 2. Treatment UI (시각적 메뉴)
**레이아웃**:
```
┌──────────────────────┐
│  [음식 사진]          │ ← 4:3 비율, 라운드 코너
│                      │
│ Margherita Pizza     │ ← 원어 (18px, bold)
│ 마르게리타 피자        │ ← 번역 (16px, regular)
│ 토마토 소스, 모짜렐라  │ ← 설명 (14px, gray, 2-3줄)
│ $15.00 (₩20,000)    │ ← 가격 (16px, semibold)
└──────────────────────┘
```

**스타일링**:
- 카드 형태 (흰색, 그림자 효과)
- 이미지 없으면 placeholder 표시
- 간격: 카드 사이 16px

---

### 3. 설문 모달
**질문**: "이 정보만으로 확신을 갖고 주문할 수 있습니까?"

**스타일링**:
- 반투명 오버레이 (rgba(0,0,0,0.5))
- 모달: 흰색, 중앙 정렬, 라운드 코너
- 버튼: 크기 동일, 충분한 터치 영역 (최소 48x48px)

**동작**:
- 5초 후 자동 표시
- Yes/No 클릭 → POST /api/surveys → "감사합니다!" → 닫기
- 백드롭 클릭 시 무시 (반드시 응답해야 함)

---

### 4. 업로더 UI
**이미지 업로드 영역**:
- 드래그앤드롭 지원
- 클릭하여 파일 선택
- 미리보기 표시
- 허용 포맷: JPG, PNG, HEIC
- 최대 크기: 10MB

**버튼**:
- "Scan Menu" (primary button)
- 비활성 상태: 이미지 미선택 시
- 로딩 상태: "Processing..." 표시

---

### 5. 로딩 인디케이터
- 전체 화면 오버레이
- 중앙 정렬 스피너
- 메시지: "메뉴를 분석하는 중..."
- 예상 시간: "보통 5초 정도 걸립니다."

---

### 6. 에러 메시지
- 빨간색 배경 (light red)
- 아이콘: ⚠️
- 메시지: 명확하고 간결
- 액션 버튼: "다시 시도"

---

## 반응형 디자인

| 디바이스 | 브레이크포인트 | 레이아웃 |
|---------|---------------|----------|
| 모바일   | < 640px       | 1열      |
| 태블릿   | 640px - 1024px | 2열     |
| 데스크톱 | > 1024px      | 2-3열    |

**모바일 우선**:
- 여행객은 주로 스마트폰 사용
- 터치 인터랙션 최적화
- 모든 버튼 최소 48x48px

---

## 코드 예시

### API Client (lib/api.ts)
```typescript
import axios from 'axios';

export const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080',
  withCredentials: true,  // 세션 쿠키 포함 (중요!)
  headers: {
    'Content-Type': 'application/json',
  },
});

export async function scanMenu(request: MenuScanRequest): Promise<MenuScanResponse> {
  const response = await api.post<MenuScanResponse>('/api/menus/scan', request);
  return response.data;
}

export async function submitSurvey(scanId: string, hasConfidence: boolean): Promise<void> {
  await api.post('/api/surveys', { scanId, hasConfidence });
}

export async function getLanguages(): Promise<LanguageResponse[]> {
  const response = await api.get<LanguageResponse[]>('/api/language');
  return response.data;
}

export async function getCurrencies(): Promise<CurrencyResponse[]> {
  const response = await api.get<CurrencyResponse[]>('/api/currency');
  return response.data;
}
```

---

### Custom Hook (hooks/useMenuScan.ts)
```typescript
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

### 조건부 렌더링 (MenuResultPage.tsx)
```typescript
function MenuResultPage() {
  const { scanId } = useParams();
  const [result, setResult] = useState<MenuScanResponse | null>(null);

  // ... API 호출 ...

  if (!result) return <LoadingSpinner />;

  return (
    <div>
      {result.abGroup === 'CONTROL' ? (
        <ControlMenu items={result.items} />
      ) : (
        <TreatmentMenu items={result.items} />
      )}

      <SurveyModal scanId={result.scanId} delay={5000} />
    </div>
  );
}
```

---

### Base64 변환 (utils/imageUtils.ts)
```typescript
export function fileToBase64(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => {
      const base64 = reader.result as string;
      // "data:image/jpeg;base64," 제거
      const base64Data = base64.split(',')[1];
      resolve(base64Data);
    };
    reader.onerror = reject;
    reader.readAsDataURL(file);
  });
}

export function validateImageSize(file: File): boolean {
  const maxSize = 10 * 1024 * 1024; // 10MB
  return file.size <= maxSize;
}

export function validateImageType(file: File): boolean {
  const allowedTypes = ['image/jpeg', 'image/png', 'image/heic'];
  return allowedTypes.includes(file.type);
}
```

---

## TypeScript 타입 정의 (lib/types.ts)

```typescript
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
  currencyCode: string;
}

export interface ApiError {
  error: string;
  message: string;
  timestamp: string;
}
```

---

## 구현 체크리스트

### Phase 1: 프로젝트 셋업
- [ ] React + TypeScript + Vite (또는 Next.js) 프로젝트 생성
- [ ] Tailwind CSS 설치 및 설정
- [ ] React Router (또는 Next.js App Router) 설정
- [ ] Axios 설치
- [ ] 환경변수 설정 (`.env.local`)

### Phase 2: API 레이어
- [ ] `lib/api.ts`: API 클라이언트 구현 (scanMenu, submitSurvey, getLanguages, getCurrencies)
- [ ] `lib/types.ts`: TypeScript 타입 정의
- [ ] `utils/imageUtils.ts`: 이미지 처리 유틸
- [ ] `utils/errorHandler.ts`: 에러 처리 유틸

### Phase 3: 커스텀 훅
- [ ] `hooks/useMenuScan.ts`: 메뉴 스캔 훅
- [ ] `hooks/useSurvey.ts`: 설문 제출 훅
- [ ] `hooks/useLanguages.ts`: 언어 목록 조회 훅
- [ ] `hooks/useCurrencies.ts`: 화폐 목록 조회 훅

### Phase 4: 공통 컴포넌트
- [ ] `LoadingSpinner.tsx`: 로딩 UI
- [ ] `ErrorMessage.tsx`: 에러 메시지 UI
- [ ] `MenuUploader.tsx`: 이미지 업로드 UI
- [ ] `LanguageSelector.tsx`: 언어 선택 드롭다운
- [ ] `CurrencySelector.tsx`: 화폐 선택 드롭다운

### Phase 5: 핵심 컴포넌트
- [ ] `ControlMenu.tsx`: Control 그룹 UI (텍스트만)
- [ ] `TreatmentMenu.tsx`: Treatment 그룹 UI (사진 + 설명)
- [ ] `SurveyModal.tsx`: 설문 모달 (5초 후 자동 표시)

### Phase 6: 페이지
- [ ] `HomePage.tsx`: 업로드 페이지 (`/`)
- [ ] `MenuResultPage.tsx`: 메뉴 결과 페이지 (`/menu/:scanId`)

### Phase 7: 테스트 및 최적화
- [ ] 로컬 환경에서 백엔드 없이 Mock API로 테스트
- [ ] 반응형 디자인 테스트 (모바일, 태블릿, 데스크톱)
- [ ] 에러 핸들링 테스트
- [ ] 성능 최적화 (이미지 lazy loading 등)

---

## DO (필수)

✅ **A/B 테스트 무결성 유지**:
- `abGroup`에 따라 **정확히** Control 또는 Treatment UI 렌더링
- 사용자가 그룹을 변경할 수 없어야 함

✅ **API 스펙 준수**:
- Request/Response 타입 정확히 일치
- `withCredentials: true` 반드시 설정 (세션 관리)

✅ **모바일 우선**:
- 반응형 디자인 필수
- 터치 영역 최소 48x48px

✅ **에러 핸들링**:
- 모든 API 호출에 try-catch
- 사용자 친화적인 에러 메시지

✅ **성능 목표**:
- 첫 화면 로딩: < 2초
- API 응답 대기: < 5초

---

## DO NOT (금지)

❌ **MVP 범위 초과**:
- 로그인/회원 기능 추가하지 말 것
- 장바구니 기능 추가하지 말 것
- 메뉴 저장/히스토리 추가하지 말 것

❌ **A/B 테스트 무결성 훼손**:
- 사용자가 그룹을 선택하거나 변경할 수 없어야 함
- Control/Treatment UI를 임의로 수정하지 말 것

❌ **API 스펙 임의 변경**:
- 백엔드 API 스펙은 확정됨
- 필드명, 타입 변경하지 말 것

---

## Mock API (백엔드 완성 전)

백엔드가 완성되기 전까지 **MSW (Mock Service Worker)** 사용 권장:

```bash
npm install msw --save-dev
npx msw init public/ --save
```

```typescript
// mocks/handlers.ts
import { http, HttpResponse } from 'msw';

export const handlers = [
  http.post('http://localhost:8080/api/menus/scan', () => {
    return HttpResponse.json({
      scanId: '550e8400-e29b-41d4-a716-446655440000',
      abGroup: Math.random() > 0.5 ? 'TREATMENT' : 'CONTROL',
      items: [
        {
          id: '660e8400-e29b-41d4-a716-446655440001',
          originalName: 'Margherita Pizza',
          translatedName: '마르게리타 피자',
          description: '토마토 소스, 모짜렐라 치즈, 바질',
          imageUrl: 'https://via.placeholder.com/400x300',
          priceInfo: {
            originalAmount: 15.0,
            originalCurrency: 'USD',
            originalFormatted: '$15.00',
            convertedAmount: 20000,
            convertedCurrency: 'KRW',
            convertedFormatted: '₩20,000',
          },
          matchConfidence: 0.85,
        },
      ],
      processingTime: 4.2,
    });
  }),

  http.post('http://localhost:8080/api/surveys', () => {
    return HttpResponse.json({
      success: true,
      message: 'Survey response recorded successfully.',
    });
  }),
];
```

---

## 환경변수 (.env.local)

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

---

## 참고 문서

- **백엔드 API 스펙**: `backend/docs/API_SPEC.md`
- **프로젝트 1-Pager**: `docs/1-PAGER.md`
- **요구사항 명세 (PRD)**: `docs/PRD.md`

---

## 성공 기준

### 기능 검증
- [ ] 이미지 업로드 → API 호출 → 결과 표시 (E2E 플로우)
- [ ] Control 그룹 UI: 사진/설명 없이 텍스트 + 가격만 표시
- [ ] Treatment 그룹 UI: 사진 + 설명 + 텍스트 + 가격 표시
- [ ] 설문 모달: 5초 후 자동 표시 → Yes/No 제출 성공

### 품질 검증
- [ ] 모바일 반응형 동작 (iPhone, Android)
- [ ] 에러 핸들링 (이미지 크기 초과, 네트워크 오류 등)
- [ ] 로딩 상태 표시
- [ ] TypeScript 타입 오류 없음

---

## 타임라인 (권장)

| 단계 | 소요 시간 | 작업 내용 |
|-----|----------|----------|
| **Day 1** | 2-3시간 | 프로젝트 셋업 + API 레이어 + 타입 정의 |
| **Day 2** | 3-4시간 | 공통 컴포넌트 (Uploader, Selectors, Loading, Error) |
| **Day 3** | 3-4시간 | 핵심 컴포넌트 (Control/Treatment Menu, Survey Modal) |
| **Day 4** | 2-3시간 | 페이지 조립 + 라우팅 + 통합 테스트 |
| **Day 5** | 1-2시간 | 반응형 디자인 + 스타일 개선 + 최종 QA |

---

## 질문이 있다면?

- **API 스펙 변경이 필요하면**: 백엔드 팀과 협의 필요
- **UI/UX 수정이 필요하면**: A/B 테스트 무결성을 해치지 않는지 확인
- **기능 추가가 필요하면**: MVP 범위 내인지 검토

---

**이제 프론트엔드 개발을 시작하세요! 🚀**

모든 API 스펙이 명확하므로 백엔드 완성 전에도 Mock API로 독립 개발 가능합니다.
