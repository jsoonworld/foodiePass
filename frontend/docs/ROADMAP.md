# Frontend Development Roadmap

**Last Updated**: 2025-11-06
**Current Phase**: Phase 3 (구현 완료)
**Status**: Ready for Phase 4 (E2E Testing & Deployment)

---

## Phase Overview

| Phase | Status | Duration | Description |
|-------|--------|----------|-------------|
| Phase 1 | ✅ Complete | Day 1-2 | AI 분석 및 전략 수립 |
| Phase 2 | ✅ Complete | Day 3-5 | 상세 설계 (Blueprint) |
| Phase 3 | ✅ Complete | Day 6-9 | 구현 및 단위 검증 + 최적화 |
| Phase 4 | 🔄 Next | Day 10 | E2E 테스트 및 배포 |

---

## Completed Work ✅

### Phase 3: Implementation & Optimization

#### Core Features
- [x] **F-01**: 웹 업로더 (MenuUploader 컴포넌트)
  - 드래그 앤 드롭 지원
  - 이미지 타입 검증 (JPG, PNG, HEIC)
  - 파일 크기 검증 (최대 10MB)
  - 미리보기 기능

- [x] **F-06**: 시각적 메뉴 UI (TreatmentMenu 컴포넌트)
  - 음식 사진 + 설명 표시
  - 카드 형식 디자인
  - 모바일 반응형 (그리드 레이아웃)

- [x] **F-07**: 텍스트 전용 UI (ControlMenu 컴포넌트)
  - 텍스트 + 가격만 표시
  - 리스트 형식 디자인

- [x] **F-09**: 확신도 설문 (SurveyModal 컴포넌트)
  - 5초 후 자동 표시
  - Yes/No 버튼
  - API 제출 및 성공 메시지

- [x] **A/B Test Integration**
  - 백엔드 `abGroup` 기반 UI 분기
  - Control vs Treatment 정확한 렌더링

#### Performance Optimizations
- [x] 이미지 압축 (1920x1920, JPEG 85%)
- [x] Lazy loading (Treatment UI 이미지)
- [x] React Query 설정 최적화
- [x] Component memoization (React.memo)
- [x] Error Boundary
- [x] API Interceptors (에러 처리, 로깅)

#### Infrastructure
- [x] Vite + React + TypeScript 프로젝트 초기화
- [x] shadcn/ui 컴포넌트 라이브러리 통합
- [x] React Router 설정
- [x] Axios API 클라이언트
- [x] React Query 통합

---

## Phase 4: E2E Testing & Deployment 🔄

**Timeline**: Day 10 (1 day)
**Goal**: 프로덕션 배포 준비 완료

### Tasks

#### 1. E2E Testing Setup
- [ ] **Playwright 설치 및 설정**
  ```bash
  npm install -D @playwright/test
  npx playwright install
  ```

- [ ] **테스트 시나리오 작성**
  - Control 그룹 사용자 플로우
  - Treatment 그룹 사용자 플로우
  - 에러 상황 처리
  - 설문 제출 플로우

- [ ] **테스트 파일 생성**
  ```
  tests/
  ├── e2e/
  │   ├── control-flow.spec.ts
  │   ├── treatment-flow.spec.ts
  │   ├── survey.spec.ts
  │   └── error-handling.spec.ts
  ```

#### 2. A/B Test Validation
- [ ] **Control 그룹 검증**
  - 사진/설명이 표시되지 않는지 확인
  - 텍스트 + 가격만 표시되는지 확인

- [ ] **Treatment 그룹 검증**
  - 사진 + 설명 + 텍스트 + 가격 모두 표시되는지 확인
  - 이미지 lazy loading 작동 확인

- [ ] **그룹 배정 무결성**
  - 세션 내에서 그룹이 변경되지 않는지 확인
  - 새 세션에서 랜덤하게 배정되는지 확인

#### 3. Cross-Browser Testing
- [ ] Chrome (Desktop)
- [ ] Chrome (Mobile)
- [ ] Safari (iOS)
- [ ] Firefox

#### 4. Performance Testing
- [ ] **Lighthouse 점수 측정**
  - Performance: > 90
  - Accessibility: > 90
  - Best Practices: > 90
  - SEO: > 80

- [ ] **Core Web Vitals 측정**
  - LCP (Largest Contentful Paint): < 2.5s
  - FID (First Input Delay): < 100ms
  - CLS (Cumulative Layout Shift): < 0.1

- [ ] **Network Performance**
  - 이미지 압축 효과 검증
  - API 응답 시간 측정

#### 5. Deployment
- [ ] **환경 변수 설정**
  ```bash
  VITE_API_URL=<production-backend-url>
  NODE_ENV=production
  ```

- [ ] **빌드 및 검증**
  ```bash
  npm run build
  npm run preview  # 로컬에서 프로덕션 빌드 테스트
  ```

- [ ] **호스팅 배포**
  - Option 1: Vercel (권장)
  - Option 2: Netlify
  - Option 3: AWS S3 + CloudFront

- [ ] **도메인 연결**
  - DNS 설정
  - HTTPS 인증서 설정

- [ ] **모니터링 설정**
  - Sentry (에러 모니터링)
  - Google Analytics (사용자 분석)

---

## Post-MVP: Future Enhancements 📋

### Phase 5: User Testing & Iteration (Week 2)

#### User Testing
- [ ] **A/B 테스트 실행**
  - 최소 100명 이상 참여
  - Control vs Treatment 그룹별 50명 이상
  - 설문 응답률 추적

- [ ] **데이터 수집**
  - 확신도 Yes 응답률
  - 처리 시간 평균
  - OCR/매칭 정확도
  - 사용자 피드백

- [ ] **가설 검증**
  - H1: 시각적 메뉴가 확신도 향상시키는가?
  - H2: 기술적 목표 달성했는가? (OCR 90%, 환율 95%, 매칭 70%)
  - H3: Treatment / Control 확신도 비율 ≥ 2.0인가?

#### Iteration Based on Results
- [ ] **성공 시**
  - v2 기능 추가 계획 (장바구니, 주문서)
  - 추가 언어 지원
  - 더 많은 음식 DB 통합

- [ ] **실패 시**
  - 기술 R&D (OCR/매칭 정확도 개선)
  - UI/UX 개선
  - 다른 가설 수립

---

### Phase 6: Feature Expansion (Week 3-4)

**Prerequisites**: Phase 5 가설 검증 성공

#### v2 Features (Out of Scope for MVP)
- [ ] **장바구니 기능**
  - 메뉴 아이템 추가/제거
  - 수량 조절
  - 총 가격 계산

- [ ] **현지 언어 주문서**
  - 선택한 메뉴를 현지 언어로 변환
  - 인쇄 또는 화면 표시 기능
  - 음성 읽기 지원

- [ ] **로그인/회원 시스템**
  - 소셜 로그인 (Google, Kakao)
  - 주문 히스토리
  - 즐겨찾기 기능

- [ ] **레스토랑 정보**
  - 위치 기반 레스토랑 검색
  - 리뷰 및 평점
  - 영업시간, 연락처

---

### Phase 7: Technical Debt & Quality (Ongoing)

#### Code Quality
- [ ] **Test Coverage**
  - Unit tests: > 80%
  - Integration tests: Key user flows
  - E2E tests: Critical paths

- [ ] **Documentation**
  - Component storybook
  - API documentation
  - Developer onboarding guide

- [ ] **Linting & Formatting**
  - ESLint rules 강화
  - Prettier 설정
  - Husky pre-commit hooks

#### Performance
- [ ] **Code Splitting**
  - Route-based splitting
  - Component lazy loading
  - Vendor bundle optimization

- [ ] **Bundle Size Optimization**
  - 미사용 shadcn/ui 컴포넌트 제거
  - Tree shaking 검증
  - Dynamic imports

- [ ] **Image Optimization**
  - WebP 포맷 지원
  - Responsive images
  - Image CDN 통합

#### Accessibility (a11y)
- [ ] **ARIA Attributes**
  - Semantic HTML
  - Screen reader 지원
  - Focus management

- [ ] **Keyboard Navigation**
  - Tab order 최적화
  - Keyboard shortcuts
  - Skip links

- [ ] **Color Contrast**
  - WCAG AA 준수
  - High contrast mode
  - Dark mode 지원

---

## Technical Debt Register

### Current Debt
1. **HEIC Format Support**
   - Issue: Safari만 HEIC 네이티브 지원
   - Impact: Medium
   - Solution: heic2any 라이브러리 통합 또는 서버 측 변환
   - Priority: Low

2. **Unused UI Components**
   - Issue: shadcn/ui의 많은 컴포넌트가 미사용 상태
   - Impact: Bundle size 증가
   - Solution: 사용하지 않는 컴포넌트 제거
   - Priority: Medium

3. **Error Handling - Async Errors**
   - Issue: ErrorBoundary가 비동기 에러 캐치 불가
   - Impact: Low (현재 async 에러는 try-catch로 처리 중)
   - Solution: global error handler 추가
   - Priority: Low

4. **Deep Comparison in memo()**
   - Issue: React.memo가 shallow comparison만 수행
   - Impact: Low (현재 props 구조가 단순함)
   - Solution: Custom comparison 함수 또는 useMemo
   - Priority: Low

---

## Risk Management

### Technical Risks

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Backend API 불안정 | Medium | High | Mock API 준비, 에러 핸들링 강화 |
| 브라우저 호환성 | Low | Medium | Polyfills, Progressive enhancement |
| 성능 저하 (대용량 메뉴) | Medium | Medium | Virtual scrolling, Pagination |
| 이미지 압축 품질 저하 | Low | Medium | 압축률 조정 가능하도록 설정 |

### Business Risks

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| 가설 검증 실패 | Medium | High | 빠른 피봇 계획, 대안 준비 |
| 사용자 참여 부족 | Medium | High | 마케팅 전략, 인센티브 제공 |
| A/B 테스트 샘플 부족 | Medium | Medium | 테스트 기간 연장, 홍보 강화 |

---

## Decision Log

### 2025-11-06: Optimization Decisions

1. **Image Compression (1920x1920, 85% quality)**
   - Rationale: 모바일 화면에 충분한 품질, 70-80% 크기 감소
   - Alternative: 서버 측 압축 (더 복잡, MVP 범위 초과)

2. **React.memo() without custom comparison**
   - Rationale: 현재 props 구조가 단순 (items 배열)
   - Alternative: Deep comparison (불필요한 오버헤드)

3. **QueryClient: staleTime 5분**
   - Rationale: 메뉴 정보는 짧은 시간 내 변경되지 않음
   - Alternative: 0초 (너무 많은 재요청)

4. **ErrorBoundary: Global 레벨만 적용**
   - Rationale: MVP에서는 전역 에러 처리만으로 충분
   - Alternative: 컴포넌트별 ErrorBoundary (오버엔지니어링)

---

## Success Metrics

### Phase 4 Success Criteria
- [ ] 모든 E2E 테스트 통과
- [ ] Lighthouse 점수 > 90 (Performance)
- [ ] 프로덕션 배포 완료
- [ ] 모니터링 설정 완료

### Phase 5 Success Criteria (User Testing)
- [ ] Treatment 그룹 Yes 응답률 ≥ 70%
- [ ] Treatment / Control 비율 ≥ 2.0
- [ ] OCR 정확도 ≥ 90%
- [ ] 환율 정확도 ≥ 95%
- [ ] 음식 매칭 연관성 ≥ 70%
- [ ] 처리 시간 ≤ 5초

---

## Related Documentation

- [OPTIMIZATION_SUMMARY.md](./OPTIMIZATION_SUMMARY.md) - 최적화 작업 요약
- [API_INTEGRATION.md](./API_INTEGRATION.md) - Backend API 연동
- [COMPONENT_DESIGN.md](./COMPONENT_DESIGN.md) - 컴포넌트 구조
- [USER_FLOW.md](./USER_FLOW.md) - 사용자 플로우
- [UI_REQUIREMENTS.md](./UI_REQUIREMENTS.md) - UI/UX 요구사항
- [HYPOTHESES.md](./HYPOTHESES.md) - 핵심 가설

---

## Contact & Support

- **프로젝트 문서**: `/docs/1-PAGER.md`, `/docs/PRD.md`
- **백엔드 API 문서**: `/backend/docs/API_SPEC.md`
- **이슈 트래킹**: GitHub Issues (TBD)
