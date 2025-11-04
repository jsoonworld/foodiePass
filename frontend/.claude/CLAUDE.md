# FoodiePass Frontend - Context for Claude

## Current Task: MVP v2 Frontend Implementation

**Phase**: Phase 2 (설계) → Phase 3 (구현 시작)

**Goal**: A/B 테스트를 위한 Control/Treatment UI 구현 및 설문 시스템 연동

---

## Quick Links

### 프로젝트 전체 문서
- [Project CLAUDE.md](../../.claude/CLAUDE.md) - 전체 프로젝트 컨텍스트
- [1-Pager](../../docs/1-PAGER.md) - 가설 및 MVP 실험 설계
- [PRD](../../docs/PRD.md) - 가설 검증 요구사항 명세

### 프론트엔드 전용 문서
- [HYPOTHESES.md](../docs/HYPOTHESES.md) - 프론트엔드 개발을 위한 핵심 가설 요약
- [API_INTEGRATION.md](../docs/API_INTEGRATION.md) - 백엔드 API 연동 방법
- [COMPONENT_DESIGN.md](../docs/COMPONENT_DESIGN.md) - 컴포넌트 구조 및 설계
- [USER_FLOW.md](../docs/USER_FLOW.md) - 사용자 플로우 및 화면 전환
- [UI_REQUIREMENTS.md](../docs/UI_REQUIREMENTS.md) - UI/UX 요구사항

### 백엔드 API 문서
- [Backend API_SPEC.md](../../backend/docs/API_SPEC.md) - REST API 엔드포인트 상세

---

## Core Hypotheses (Reminder)

### H1: 핵심 가치 가설
> "여행객은 [텍스트 번역만]으로는 여전히 불안하지만, [사진/설명/환율]이 포함된 시각적 메뉴가 제공될 경우 '주문 확신'을 갖게 된다."

**프론트엔드의 역할**:
- Control UI: 텍스트 + 환율만 표시
- Treatment UI: 사진 + 설명 + 텍스트 + 환율 표시

### H3: 사용자 행동/인지 가설
> "[시각적 메뉴] 사용 집단은 [텍스트 번역만] 집단 대비 '확신도'가 2배 이상 높다."

**프론트엔드의 역할**:
- 설문 UI: "이 정보만으로 확신을 갖고 주문할 수 있습니까? (Yes/No)"
- abGroup에 따라 정확히 다른 UI 렌더링

---

## Tech Stack (권장)

| 레이어 | 기술 | 이유 |
|---|---|---|
| **Framework** | Next.js 14 (App Router) | SEO, SSR, 파일 기반 라우팅 |
| **Language** | TypeScript | 타입 안전성 |
| **Styling** | Tailwind CSS | 빠른 프로토타입 |
| **State** | React Context or Zustand | 간단한 상태 관리 |
| **API Client** | Axios | REST API 연동 |
| **Hosting** | Vercel or AWS S3+CloudFront | 정적 호스팅 |

---

## 디렉토리 구조

```
frontend/
├── src/
│   ├── app/                      # Next.js App Router
│   │   ├── page.tsx              # 홈 (업로더)
│   │   ├── menu/[scanId]/page.tsx # 메뉴 표시
│   │   └── layout.tsx
│   ├── components/
│   │   ├── MenuUploader/         # F-01: 업로더
│   │   ├── MenuDisplay/
│   │   │   ├── TextOnlyMenu.tsx  # F-07: Control UI
│   │   │   └── VisualMenu.tsx    # F-06: Treatment UI
│   │   ├── Survey/
│   │   │   └── SurveyModal.tsx   # F-09: 설문
│   │   └── common/
│   │       └── LoadingSpinner.tsx
│   ├── lib/
│   │   ├── api.ts                # API 클라이언트
│   │   └── types.ts              # TypeScript 타입
│   └── hooks/
│       ├── useMenuScan.ts
│       └── useSurvey.ts
├── public/
└── .env.local                    # API_BASE_URL 등
```

---

## MVP 범위 (In Scope)

### ✅ 구현 필요
- **F-01**: 웹 업로더 (이미지 + 언어/화폐 선택)
- **F-06**: 시각적 메뉴 UI (Treatment 그룹)
- **F-07**: 텍스트 전용 UI (Control 그룹)
- **F-09**: 확신도 설문 UI

### ❌ 제외 (Out of Scope)
- 로그인/회원 기능
- 장바구니
- 메뉴 저장/히스토리
- 소셜 공유

**제외 기준**: 핵심 가설(H1, H3) 검증에 직접적인 영향이 없는 기능

---

## 핵심 원칙

### 1. A/B 테스트 무결성
- 백엔드가 제공한 `abGroup`에 따라 **정확히** Control 또는 Treatment UI 렌더링
- 사용자가 그룹을 인지하거나 변경할 수 없어야 함
- 두 UI의 차이는 **오직** 사진/설명 유무

### 2. 단순성 우선 (Simplicity)
- 복잡한 기능 불필요 (MVP는 3개 화면만)
- 업로드 → 결과 표시 → 설문 제출 플로우

### 3. 모바일 우선 (Mobile-First)
- 여행객은 주로 모바일 사용
- 반응형 디자인 필수
- 터치 인터랙션 최적화

### 4. 성능 목표
- 첫 화면 로딩: < 2초
- API 응답 대기: < 5초 (백엔드 목표)
- 설문 제출: < 1초

---

## 주요 컴포넌트

### 1. MenuUploader (F-01)
**역할**: 메뉴판 이미지 업로드 + 언어/화폐 선택

**구현 요구사항**:
- 이미지 업로드 (드래그앤드롭 또는 파일 선택)
- Base64 인코딩
- 언어 선택 드롭다운 (기본값: ko)
- 화폐 선택 드롭다운 (기본값: KRW)
- "Scan Menu" 버튼
- 로딩 상태 표시

---

### 2. TextOnlyMenu (F-07, Control)
**역할**: 텍스트 + 환율만 표시

**구현 요구사항**:
- 메뉴명 (원어 + 번역)
- 가격 (원래 화폐 + 변환된 화폐)
- **사진/설명 없음**
- 간단한 리스트 형식

---

### 3. VisualMenu (F-06, Treatment)
**역할**: 사진 + 설명 + 텍스트 + 환율 표시

**구현 요구사항**:
- 음식 사진 (4:3 비율, 없으면 placeholder)
- 메뉴명 (원어 + 번역)
- 간략 설명 (2-3줄)
- 가격 (원래 화폐 + 변환된 화폐)
- 카드 형식 (그림자 효과)

---

### 4. SurveyModal (F-09)
**역할**: 확신도 설문 표시 및 제출

**구현 요구사항**:
- 질문: "이 정보만으로 확신을 갖고 주문할 수 있습니까?"
- Yes/No 버튼
- 5초 후 자동 표시
- 응답 후 API 제출 → "감사합니다!" → 모달 닫기

---

## API 연동

### Base URL
```typescript
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
```

### 주요 API

**POST /api/menus/scan**:
- Request: `{ image, targetLanguage, targetCurrency }`
- Response: `{ scanId, abGroup, items[], processingTime }`

**POST /api/surveys**:
- Request: `{ scanId, hasConfidence }`
- Response: `{ success, message }`

**세션 관리**:
- `withCredentials: true` (Axios 설정)
- 백엔드 세션 쿠키 자동 전송

---

## 개발 Workflow

### 1. Before You Start
```bash
cd frontend
npm install
```

### 2. Run Dev Server
```bash
npm run dev
# http://localhost:3000
```

### 3. Environment Variables
`.env.local`:
```
NEXT_PUBLIC_API_URL=http://localhost:8080
```

---

## DO

✅ abGroup에 따라 정확히 다른 UI 렌더링 (Control vs Treatment)
✅ 모바일 반응형 디자인 (Mobile-First)
✅ API 에러 처리 (사용자 친화적 메시지)
✅ 로딩 상태 표시 (LoadingSpinner)
✅ 설문 5초 후 자동 표시
✅ TypeScript 타입 정의
✅ 간단한 상태 관리 (Context 또는 Zustand)

---

## DO NOT

❌ 가설 검증과 무관한 기능 추가 (장바구니, 로그인 등)
❌ 사용자가 A/B 그룹 변경 가능하게 만들기
❌ Control UI에 사진/설명 포함
❌ 복잡한 상태 관리 (Redux 등 불필요)
❌ Over-engineering (MVP에 맞지 않는 복잡한 구조)

---

## 핵심 위험 및 완화

### A/B 테스트 무결성 위험
**위험**: Control/Treatment UI가 정확히 구분되지 않을 위험

**완화**:
- abGroup 값에 따라 정확히 분기
- 조건부 렌더링 (`abGroup === 'CONTROL' ? <TextOnlyMenu/> : <VisualMenu/>`)
- 테스트: 두 그룹 각각 수동 검증

### 모바일 UX 위험
**위험**: 모바일에서 사용성이 떨어질 위험

**완화**:
- 모바일 우선 디자인
- 충분한 터치 영역 (최소 48x48px)
- 반응형 브레이크포인트 설정

---

## Next Steps

### Phase 3: 구현 (Day 6-9)
1. **Day 6**: 프로젝트 초기화 (Next.js) + API 클라이언트
2. **Day 7**: MenuUploader 컴포넌트 구현
3. **Day 8**: Control/Treatment UI 구현
4. **Day 9**: SurveyModal 구현 및 통합 테스트

### Phase 4: E2E 테스트 및 배포 (Day 10)
- E2E 테스트 (Playwright)
- A/B 테스트 검증
- 배포 (Vercel)

---

## Notes for Claude

- 이 프로젝트는 **A/B 테스트가 핵심**입니다
- Control vs Treatment UI를 **정확히** 구분하여 구현해야 합니다
- **모바일 우선** 설계 (여행객은 주로 스마트폰 사용)
- MVP 범위를 **엄격히 준수**합니다 (3개 화면만)
- 백엔드 API는 이미 설계되어 있습니다 ([API_SPEC.md](../../backend/docs/API_SPEC.md) 참조)
- **10일 타임라인**이 매우 타이트하므로 효율성이 중요합니다
