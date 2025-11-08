# FoodiePass Backend - API Specification

> **목적**: REST API 엔드포인트 상세 명세 (프론트엔드 연동용)

---

## Base URL

| 환경 | Base URL |
|---|---|
| **Local** | `http://localhost:8080` |
| **Dev** | `https://dev-api.foodiepass.com` |
| **Prod** | `https://api.foodiepass.com` |

---

## API Endpoints

### 1. 메뉴 스캔 API

#### POST /api/menus/scan

**목적**: 메뉴판 이미지 업로드 및 A/B 테스트 그룹 배정 후 파이프라인 실행

**검증 가설**: H1, H2, H3 (A/B 테스트 및 기술 검증)

---

**Request Headers**:
```http
Content-Type: application/json
Cookie: JSESSIONID=<session-id>
```text

**Request Body**:
```json
{
  "base64EncodedImage": "base64 encoded image string",
  "originLanguageName": "English",     // Optional, default: "auto"
  "userLanguageName": "Korean",        // Required
  "originCurrencyName": "USD Dollar",  // Optional, auto-detect 가능
  "userCurrencyName": "KRW Won"        // Required
}
```text

**Request Parameters**:

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `base64EncodedImage` | String (Base64) | Y | 메뉴판 이미지 (Base64 인코딩) |
| `originLanguageName` | String | N | 원본 언어명 (예: "English", "auto") |
| `userLanguageName` | String | Y | 사용자 언어명 (예: "Korean", "English", "Japanese") |
| `originCurrencyName` | String | N | 원본 화폐명 (예: "USD Dollar", auto-detect 가능) |
| `userCurrencyName` | String | Y | 사용자 화폐명 (예: "KRW Won", "USD Dollar", "JPY Yen") |

**참고**: 기존 v1 API와의 호환성을 위해 언어/화폐 필드명은 ReconfigureRequest와 동일하게 유지됩니다.

---

**Response (Treatment 그룹)**:
```json
{
  "scanId": "550e8400-e29b-41d4-a716-446655440000",
  "abGroup": "TREATMENT",
  "items": [
    {
      "id": "660e8400-e29b-41d4-a716-446655440001",
      "originalName": "Margherita Pizza",
      "translatedName": "마르게리타 피자",
      "description": "토마토 소스, 모짜렐라 치즈, 신선한 바질",
      "imageUrl": "https://cdn.tasteatlas.com/images/dishes/margherita-pizza.jpg",
      "priceInfo": {
        "originalAmount": 15.00,
        "originalCurrency": "USD",
        "originalFormatted": "$15.00",
        "convertedAmount": 20000,
        "convertedCurrency": "KRW",
        "convertedFormatted": "₩20,000"
      },
      "matchConfidence": 0.85
    },
    {
      "id": "660e8400-e29b-41d4-a716-446655440002",
      "originalName": "Pasta Carbonara",
      "translatedName": "까르보나라 파스타",
      "description": "계란, 페코리노 치즈, 판체타, 후추",
      "imageUrl": "https://cdn.tasteatlas.com/images/dishes/carbonara.jpg",
      "priceInfo": {
        "originalAmount": 18.00,
        "originalCurrency": "USD",
        "originalFormatted": "$18.00",
        "convertedAmount": 24000,
        "convertedCurrency": "KRW",
        "convertedFormatted": "₩24,000"
      },
      "matchConfidence": 0.92
    }
  ],
  "processingTime": 4.2
}
```text

**Response (Control 그룹)**:
```json
{
  "scanId": "550e8400-e29b-41d4-a716-446655440000",
  "abGroup": "CONTROL",
  "items": [
    {
      "id": "660e8400-e29b-41d4-a716-446655440001",
      "originalName": "Margherita Pizza",
      "translatedName": "마르게리타 피자",
      "priceInfo": {
        "originalAmount": 15.00,
        "originalCurrency": "USD",
        "originalFormatted": "$15.00",
        "convertedAmount": 20000,
        "convertedCurrency": "KRW",
        "convertedFormatted": "₩20,000"
      }
      // description, imageUrl, matchConfidence 없음
    },
    {
      "id": "660e8400-e29b-41d4-a716-446655440002",
      "originalName": "Pasta Carbonara",
      "translatedName": "까르보나라 파스타",
      "priceInfo": {
        "originalAmount": 18.00,
        "originalCurrency": "USD",
        "originalFormatted": "$18.00",
        "convertedAmount": 24000,
        "convertedCurrency": "KRW",
        "convertedFormatted": "₩24,000"
      }
    }
  ],
  "processingTime": 3.1
}
```text

**Response Fields**:

| 필드 | 타입 | 설명 |
|---|---|---|
| `scanId` | UUID | 메뉴 스캔 세션 ID (설문 제출 시 사용) |
| `abGroup` | String | A/B 그룹 ("CONTROL" or "TREATMENT") |
| `items[]` | Array | 메뉴 아이템 리스트 |
| `items[].id` | UUID | 메뉴 아이템 ID |
| `items[].originalName` | String | 원어 메뉴명 |
| `items[].translatedName` | String | 번역된 메뉴명 |
| `items[].description` | String | 음식 설명 (Treatment만) |
| `items[].imageUrl` | String | 음식 사진 URL (Treatment만) |
| `items[].priceInfo` | Object | 가격 정보 |
| `items[].priceInfo.originalAmount` | Number | 원래 가격 (숫자) |
| `items[].priceInfo.originalCurrency` | String | 원래 화폐 코드 |
| `items[].priceInfo.originalFormatted` | String | 원래 가격 (포맷팅) |
| `items[].priceInfo.convertedAmount` | Number | 변환된 가격 (숫자) |
| `items[].priceInfo.convertedCurrency` | String | 변환된 화폐 코드 |
| `items[].priceInfo.convertedFormatted` | String | 변환된 가격 (포맷팅) |
| `items[].matchConfidence` | Number | 매칭 신뢰도 (0-1, Treatment만) |
| `processingTime` | Number | 처리 시간 (초) |

---

**Error Responses**:

**400 Bad Request** (잘못된 요청):
```json
{
  "error": "BAD_REQUEST",
  "message": "Invalid image format. Only JPG, PNG, HEIC are allowed.",
  "timestamp": "2025-11-03T12:34:56Z"
}
```text

**413 Payload Too Large** (이미지 크기 초과):
```json
{
  "error": "PAYLOAD_TOO_LARGE",
  "message": "Image size exceeds 10MB limit.",
  "timestamp": "2025-11-03T12:34:56Z"
}
```text

**500 Internal Server Error** (서버 오류):
```json
{
  "error": "INTERNAL_SERVER_ERROR",
  "message": "OCR processing failed. Please try again.",
  "timestamp": "2025-11-03T12:34:56Z"
}
```text

---

**처리 로직**:
1. 세션 ID 생성 또는 가져오기
2. A/B 그룹 배정 (랜덤 50:50, Redis 캐싱)
3. OCR 실행 (Google Vertex AI)
4. 번역 실행 (Google Translation)
5. 환율 변환 (Currency API)
6. **조건부**: Treatment 그룹만 음식 매칭 실행 (TasteAtlas)
7. MenuScan 레코드 저장
8. 응답 반환 (abGroup에 따라 다른 필드 포함)

---

### 2. 설문 제출 API

#### POST /api/surveys

**목적**: 확신도 설문 응답 수집

**검증 가설**: H1, H3 (사용자 확신도 측정)

---

**Request Headers**:
```http
Content-Type: application/json
```text

**Request Body**:
```json
{
  "scanId": "550e8400-e29b-41d4-a716-446655440000",
  "hasConfidence": true
}
```text

**Request Parameters**:

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `scanId` | UUID | Y | 메뉴 스캔 세션 ID (`/api/menus/scan`에서 받은 값) |
| `hasConfidence` | Boolean | Y | 확신 여부 (Yes=true, No=false) |

---

**Response**:
```json
{
  "success": true,
  "message": "Survey response recorded successfully."
}
```text

**Response Fields**:

| 필드 | 타입 | 설명 |
|---|---|---|
| `success` | Boolean | 성공 여부 |
| `message` | String | 결과 메시지 |

---

**Error Responses**:

**404 Not Found** (scanId가 존재하지 않음):
```json
{
  "error": "NOT_FOUND",
  "message": "Menu scan not found with id: 550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2025-11-03T12:34:56Z"
}
```text

**400 Bad Request** (중복 응답):
```json
{
  "error": "BAD_REQUEST",
  "message": "Survey response already submitted for this scan.",
  "timestamp": "2025-11-03T12:34:56Z"
}
```bash

---

### 3. A/B 테스트 결과 API (관리자용)

#### GET /api/admin/ab-test/results

**목적**: A/B 테스트 결과 분석 (Control vs Treatment 응답률 비교)

**검증 가설**: H3 (사용자 행동/인지 가설)

---

**Request Headers**:
```http
Authorization: Bearer <admin-token>
```text

**Query Parameters**:

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `startDate` | String (ISO8601) | N | 시작 날짜 (기본값: 전체) |
| `endDate` | String (ISO8601) | N | 종료 날짜 (기본값: 현재) |

---

**Response**:
```json
{
  "control": {
    "totalResponses": 10,
    "yesCount": 3,
    "noCount": 7,
    "yesRate": 0.30
  },
  "treatment": {
    "totalResponses": 10,
    "yesCount": 8,
    "noCount": 2,
    "yesRate": 0.80
  },
  "comparison": {
    "ratio": 2.67,
    "hypothesisValidated": true
  },
  "metadata": {
    "startDate": "2025-11-01T00:00:00Z",
    "endDate": "2025-11-10T23:59:59Z",
    "generatedAt": "2025-11-10T12:00:00Z"
  }
}
```text

**Response Fields**:

| 필드 | 타입 | 설명 |
|---|---|---|
| `control.totalResponses` | Number | Control 그룹 총 응답 수 |
| `control.yesCount` | Number | Control 그룹 Yes 응답 수 |
| `control.noCount` | Number | Control 그룹 No 응답 수 |
| `control.yesRate` | Number | Control 그룹 Yes 응답률 (0-1) |
| `treatment.totalResponses` | Number | Treatment 그룹 총 응답 수 |
| `treatment.yesCount` | Number | Treatment 그룹 Yes 응답 수 |
| `treatment.noCount` | Number | Treatment 그룹 No 응답 수 |
| `treatment.yesRate` | Number | Treatment 그룹 Yes 응답률 (0-1) |
| `comparison.ratio` | Number | Treatment / Control Yes 응답률 비율 |
| `comparison.hypothesisValidated` | Boolean | H3 검증 여부 (ratio ≥ 2.0) |
| `metadata.startDate` | String | 분석 시작 날짜 |
| `metadata.endDate` | String | 분석 종료 날짜 |
| `metadata.generatedAt` | String | 결과 생성 시각 |

---

**Error Responses**:

**401 Unauthorized** (인증 실패):
```json
{
  "error": "UNAUTHORIZED",
  "message": "Invalid or missing admin token.",
  "timestamp": "2025-11-03T12:34:56Z"
}
```bash

---

## Common Error Codes

| HTTP Status | Error Code | 설명 |
|---|---|---|
| 400 | `BAD_REQUEST` | 잘못된 요청 (파라미터 오류, 유효성 검증 실패) |
| 401 | `UNAUTHORIZED` | 인증 실패 |
| 404 | `NOT_FOUND` | 리소스를 찾을 수 없음 |
| 413 | `PAYLOAD_TOO_LARGE` | 요청 크기 초과 |
| 429 | `TOO_MANY_REQUESTS` | Rate limit 초과 |
| 500 | `INTERNAL_SERVER_ERROR` | 서버 내부 오류 |
| 503 | `SERVICE_UNAVAILABLE` | 외부 API 연동 실패 |

---

## Rate Limiting

| 엔드포인트 | 제한 | 기준 |
|---|---|---|
| `POST /api/menus/scan` | 10 requests / min | IP 주소 |
| `POST /api/surveys` | 100 requests / min | IP 주소 |
| `GET /api/admin/ab-test/results` | 60 requests / min | API 토큰 |

**Rate Limit 초과 시 응답**:
```json
{
  "error": "TOO_MANY_REQUESTS",
  "message": "Rate limit exceeded. Please try again in 60 seconds.",
  "retryAfter": 60,
  "timestamp": "2025-11-03T12:34:56Z"
}
```bash

---

## CORS Configuration

**허용 도메인**:
- `http://localhost:3000` (로컬 개발)
- `https://foodiepass.com` (프로덕션)
- `https://dev.foodiepass.com` (개발)

**허용 메서드**: `GET`, `POST`, `OPTIONS`

**허용 헤더**: `Content-Type`, `Authorization`

---

## 프론트엔드 연동 예시

### JavaScript (Fetch API)

```javascript
// 1. 메뉴 스캔
async function scanMenu(imageBase64, userLanguageName, userCurrencyName) {
  const response = await fetch('http://localhost:8080/api/menus/scan', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include', // 세션 쿠키 포함
    body: JSON.stringify({
      base64EncodedImage: imageBase64,
      userLanguageName: userLanguageName,
      userCurrencyName: userCurrencyName,
    }),
  });

  if (!response.ok) {
    throw new Error(`Scan failed: ${response.statusText}`);
  }

  return await response.json();
}

// 2. 설문 제출
async function submitSurvey(scanId, hasConfidence) {
  const response = await fetch('http://localhost:8080/api/surveys', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      scanId: scanId,
      hasConfidence: hasConfidence,
    }),
  });

  if (!response.ok) {
    throw new Error(`Survey submission failed: ${response.statusText}`);
  }

  return await response.json();
}

// 사용 예시
const result = await scanMenu(imageBase64, 'Korean', 'KRW Won');
console.log('A/B Group:', result.abGroup);
console.log('Items:', result.items);

await submitSurvey(result.scanId, true);
```java

---

### TypeScript (Axios)

```typescript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080',
  withCredentials: true, // 세션 쿠키 포함
});

// 타입 정의
interface MenuScanRequest {
  base64EncodedImage: string;
  originLanguageName?: string;
  userLanguageName: string;
  originCurrencyName?: string;
  userCurrencyName: string;
}

interface MenuScanResponse {
  scanId: string;
  abGroup: 'CONTROL' | 'TREATMENT';
  items: MenuItem[];
  processingTime: number;
}

interface MenuItem {
  id: string;
  originalName: string;
  translatedName: string;
  description?: string;
  imageUrl?: string;
  priceInfo: PriceInfo;
  matchConfidence?: number;
}

interface PriceInfo {
  originalAmount: number;
  originalCurrency: string;
  originalFormatted: string;
  convertedAmount: number;
  convertedCurrency: string;
  convertedFormatted: string;
}

// API 호출
export async function scanMenu(request: MenuScanRequest): Promise<MenuScanResponse> {
  const response = await api.post<MenuScanResponse>('/api/menus/scan', request);
  return response.data;
}

export async function submitSurvey(scanId: string, hasConfidence: boolean): Promise<void> {
  await api.post('/api/surveys', { scanId, hasConfidence });
}
```bash

---

## 성능 목표

| 메트릭 | 목표 | 현재 |
|---|---|---|
| **처리 시간** | ≤ 5초 | TBD |
| **처리량** | 100 req/min | TBD |
| **가용성** | 99% | TBD |

---

## 참고 문서

- [ARCHITECTURE.md](./ARCHITECTURE.md) - 백엔드 아키텍처
- [DATABASE_SCHEMA.md](./DATABASE_SCHEMA.md) - DB 스키마
- [HYPOTHESES.md](./HYPOTHESES.md) - 핵심 가설 및 검증 목표
