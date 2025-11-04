# FoodiePass v2 - Context for Claude

## Project Status

- **Phase**: MVP - Hypothesis Validation (v2)
- **Branch**: `feature/monorepo-structure` (based on `develop`)
- **Current Focus**: 가설 검증을 위한 MVP 개발 (10일 일정)

## Project Vision

**핵심 문제**: 여행객이 외국어 메뉴판을 마주할 때 '정보 비대칭'으로 인한 '주문 실패의 불안'을 겪음

**솔루션**: 메뉴판 사진 → [OCR + 번역 + 사진/설명 + 환율] → 시각적 메뉴 제공

**MVP 목표**: A/B 테스트를 통해 "시각적 메뉴가 주문 확신도를 2배 이상 높이는가?" 검증

## Repository Structure

```
foodiePass/
├── backend/          # Spring Boot 3.5.3, Java 21
│   ├── docs/        # 백엔드 전용 문서
│   │   ├── HYPOTHESES.md         # 가설 요약
│   │   ├── ARCHITECTURE.md       # 아키텍처
│   │   ├── API_SPEC.md          # API 명세
│   │   ├── DATABASE_SCHEMA.md   # DB 스키마
│   │   ├── CODE_REUSE_GUIDE.md  # v1 재사용 가이드
│   │   └── IMPLEMENTATION_PLAN.md # 구현 계획
│   └── .claude/     # 백엔드 AI 컨텍스트
│
├── frontend/        # React/Next.js
│   ├── docs/       # 프론트엔드 전용 문서
│   │   ├── HYPOTHESES.md         # 가설 요약
│   │   ├── API_INTEGRATION.md    # API 연동
│   │   ├── COMPONENT_DESIGN.md   # 컴포넌트 설계
│   │   ├── USER_FLOW.md         # 사용자 플로우
│   │   └── UI_REQUIREMENTS.md   # UI/UX 요구사항
│   └── .claude/    # 프론트엔드 AI 컨텍스트
│
├── docs/           # 프로젝트 공통 문서
│   ├── 1-PAGER.md  # 프로젝트 비전 및 가설
│   └── PRD.md      # 가설 검증 계획서
│
├── scripts/        # Development scripts
└── .claude/        # 루트 AI 컨텍스트
```

## Quick Links

### 프로젝트 공통 문서
- [1-Pager](../docs/1-PAGER.md) - 프로젝트 비전, 핵심 가설, MVP 실험 설계
- [PRD](../docs/PRD.md) - 가설 검증 중심 요구사항 명세
- [Project README](../README.md) - Monorepo 개요

### 백엔드 문서
- [Backend CLAUDE.md](../backend/.claude/CLAUDE.md) - 백엔드 AI 컨텍스트
- [Backend README](../backend/README.md) - 백엔드 설정 및 실행
- [Backend docs/](../backend/docs/) - 백엔드 전용 문서들

### 프론트엔드 문서
- [Frontend CLAUDE.md](../frontend/.claude/CLAUDE.md) - 프론트엔드 AI 컨텍스트
- [Frontend README](../frontend/README.md) - 프론트엔드 설정 및 실행 (TBD)
- [Frontend docs/](../frontend/docs/) - 프론트엔드 전용 문서들

## Core Hypotheses

### H1: 핵심 가치 가설
> "여행객은 [텍스트 번역만]으로는 여전히 불안하지만, [사진/설명/환율]이 포함된 시각적 메뉴가 제공될 경우 '주문 확신'을 갖게 된다."

### H2: 기술 실현 가설
> "우리는 OCR, 환율 API, 음식 매칭 시스템을 통해 70% 이상 연관성의 사진/설명과 95% 이상 정확도의 환율을 제공할 수 있다."

### H3: 사용자 행동/인지 가설
> "[시각적 메뉴] 사용 집단은 [텍스트 번역만] 집단 대비 '확신도'가 2배 이상 높다."

## Success Criteria

### 기술 검증 (H2)
- OCR 정확도 ≥ 90%
- 환율 정확도 ≥ 95%
- 음식 매칭 연관성 ≥ 70%
- 처리 시간 ≤ 5초

### 사용자 검증 (H1, H3)
- Treatment 그룹 Yes 응답률 ≥ 70%
- Treatment / Control Yes 응답률 비율 ≥ 2.0

## MVP Scope

### ✅ In Scope
- F-01: 웹 업로더 (메뉴판 사진 + 언어/화폐 선택)
- F-02: OCR 파이프라인 (Gemini)
- F-03: 번역 파이프라인 (Google Translation)
- F-04: 음식 매칭 파이프라인 (TasteAtlas)
- F-05: 환율 변환 (Currency API)
- F-06: 시각적 메뉴 UI (Treatment 그룹)
- F-07: 텍스트 전용 UI (Control 그룹)
- F-08: A/B 그룹 배정
- F-09: 확신도 설문

### ❌ Out of Scope
- 장바구니 기능
- 현지 언어 주문서
- 로그인/회원 시스템
- 결제 시스템
- 식당 리뷰/평점

**제외 기준**: 핵심 가설(H1, H2, H3)을 검증하는 데 직접적인 영향이 없는 기능

## Tech Stack

### Backend (Existing - Reuse)
- **Framework**: Spring Boot 3.5.3
- **Language**: Java 21
- **Database**: MySQL (v2), H2 (dev)
- **Cache**: Redis (세션 관리, 환율 캐싱)
- **External APIs**:
  - Google Cloud Vertex AI (OCR) ✅ 재사용
  - Google Cloud Translation ✅ 재사용
  - TasteAtlas (food images) ✅ 재사용
  - Currency API ✅ 재사용

### Backend (New)
- A/B 테스트 시스템 (`ABTestService`)
- 설문 시스템 (`SurveyService`)
- 새 도메인: `MenuScan`, `SurveyResponse`

### Frontend (To Build)
- **Framework**: React 또는 Next.js (TBD)
- **Language**: TypeScript
- **Styling**: Tailwind CSS (TBD)
- **UI**: Control UI vs Treatment UI

## Code Organization

### Backend Architecture
```
backend/src/main/java/foodiepass/server/
├── menu/              # ✅ 재사용 (MenuService, OCR, Enrichment)
├── language/          # ✅ 재사용 (Translation)
├── currency/          # ✅ 재사용 (환율 변환)
├── abtest/            # ➕ 새로 개발 (A/B 테스트)
├── survey/            # ➕ 새로 개발 (설문)
├── order/             # ⚠️ MVP 제외
└── script/            # ⚠️ MVP 제외
```

### Domain Model
**기존 재사용**:
- `MenuItem`: 메뉴 아이템 (OCR + 번역 + 환율 + 음식 정보)
- `Currency`, `Language`: 도메인 객체

**새로 추가**:
- `MenuScan`: 메뉴 스캔 세션 (A/B 그룹 정보 포함)
- `SurveyResponse`: 확신도 설문 응답
- `ABGroup` enum: CONTROL | TREATMENT

## Current Iteration: MVP - Hypothesis Validation

**Goal**: 핵심 가설(H1, H2, H3) 검증

**Timeline**: 10일
- Phase 1 (Day 1-2): ✅ AI 분석 및 전략 수립 (완료)
- Phase 2 (Day 3-5): 상세 설계 (Blueprint)
- Phase 3 (Day 6-9): 구현 및 단위 검증
- Phase 4 (Day 10): E2E 테스트 및 배포

**Current Phase**: Phase 2 (상세 설계)

## Development Workflow

### Backend
```bash
cd backend
./gradlew bootRun --args='--spring.profiles.active=local'
```

### Frontend (once set up)
```bash
cd frontend
npm run dev
```

### Database
- **Dev**: H2 in-memory
- **Prod**: MySQL (RDS)

### Redis
- **Dev**: Docker (`docker run -p 6379:6379 redis:7`)
- **Prod**: ElastiCache

## API Design

### Key Endpoints

**POST /api/menus/scan**
- 메뉴판 업로드 → A/B 그룹 배정 → 파이프라인 실행
- Response에 `abGroup` 포함 (Control | Treatment)
- Treatment 그룹만 음식 사진/설명 포함

**POST /api/surveys**
- 확신도 설문 응답 수집 (`scanId`, `hasConfidence`)

**GET /api/admin/ab-test/results**
- A/B 테스트 결과 분석 (관리자용)

## Working Principles

### 1. Hypothesis-Driven Development
- 모든 기능은 특정 가설을 검증하기 위해 존재
- 가설 검증에 불필요한 기능은 제외
- "이 기능으로 어떤 가설을 검증하는가?" 질문 필수

### 2. Code Reuse First
- 기존 v1 코드 최대한 재사용 (`MenuService`, `CurrencyService` 등)
- 새 기능만 추가 개발 (A/B 테스트, 설문)
- 기존 도메인 구조 유지

### 3. MVP Discipline
- 범위를 엄격히 제한 (장바구니, 주문서 제외)
- 검증에 필요한 최소 기능만 구현
- "이게 정말 가설 검증에 필요한가?" 지속적으로 질문

### 4. Test-Driven
- 단위 테스트: Service 레이어 비즈니스 로직
- 통합 테스트: API 엔드포인트, 파이프라인 전체 플로우
- 성능 테스트: 처리 시간 ≤ 5초 검증

## DO

- ✅ 기존 코드 재사용 우선 (MenuService, LanguageService 등)
- ✅ 가설 검증에 집중 (H1, H2, H3)
- ✅ A/B 테스트 로직 구현 (ABTestService)
- ✅ 설문 시스템 구현 (SurveyService)
- ✅ Control vs Treatment UI 분기
- ✅ 처리 시간 5초 이내 최적화
- ✅ 테스트 작성 (>80% coverage)
- ✅ feature 브랜치 사용 (`feature/...`)

## DO NOT

- ❌ 가설 검증과 무관한 기능 추가 (장바구니, 로그인 등)
- ❌ 기존 v1 코드 불필요하게 수정
- ❌ 데이터베이스 스키마 임의 변경
- ❌ 인프라/배포 설정 (Phase 4에서 진행)
- ❌ Over-engineering (MVP에 맞지 않는 복잡한 구조)
- ❌ Marketing language 사용

## Key Risks

### Technical Risk (H2)
**위험**: OCR/매칭 정확도가 목표를 달성하지 못할 위험

**완화**:
- Phase 3 종료 시 내부 기술 테스트 먼저 진행
- 실패 시 사용자 테스트 중단, 기술 R&D로 피봇

### Measurement Risk (H3)
**위험**: 설문 응답이 실제 행동을 반영하지 못할 위험

**완화**:
- Control 그룹과의 상대적 차이로 가치 검증
- 후속 단계에서 실제 주문 행동 추적

## Next Steps

1. **Phase 2 완료**: API 설계, DB 스키마, UI 목업
2. **Phase 3 시작**: 구현 및 단위 검증
   - A/B 테스트 시스템 구현
   - 설문 시스템 구현
   - Control/Treatment UI 구현
   - 기존 파이프라인 통합
3. **Phase 4**: E2E 테스트 및 배포

## Notes for Claude

- 이 프로젝트는 **가설 검증이 최우선 목표**입니다
- 기존 v1 코드를 **최대한 재사용**하되, A/B 테스트와 설문 기능만 추가합니다
- MVP 범위를 **엄격히 준수**합니다 (장바구니, 주문서 제외)
- 모든 기능은 **특정 가설(H1, H2, H3)을 검증**하기 위해 존재합니다
- **10일 타임라인**이 매우 타이트하므로 효율성이 중요합니다
