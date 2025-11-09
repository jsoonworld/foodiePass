# BPlusTree3 방법론 적용 계획서

## 개요

Kent Beck의 BPlusTree3 프로젝트에서 사용하는 **Claude 개발 문서, 에이전트 설정, 체크리스트, 품질 표준**을 FoodiePass에 적용하기 위한 구체적인 계획입니다.

**Source**: https://github.com/KentBeck/BPlusTree3

**목표**: B+ Tree 데이터 구조가 아닌, **Kent Beck이 개발할 때 사용한 Claude 문서들과 방법론**을 그대로 가져와서 FoodiePass에 맞게 적용

## 적용 구조

### 3단계 계층 구조

```
foodiePass/                          # 루트: 헌법 (공통 원칙)
├── .claude/
│   ├── CLAUDE.md                   # 기존 (프로젝트 개요)
│   ├── PRINCIPLES.md               # 신규 (핵심 개발 원칙)
│   ├── quality_standards.md        # 신규 (품질 표준)
│   └── decision_framework.md       # 신규 (의사결정 프레임워크)
├── docs/
│   ├── adr/                        # Architecture Decision Records
│   └── analysis/                   # 분석 문서 템플릿
│
├── backend/                         # 백엔드: Java/Spring Boot 특화
│   └── .claude/
│       ├── CLAUDE.md               # 기존 (백엔드 컨텍스트)
│       ├── agent.md                # 신규 (백엔드 개발 규칙)
│       ├── pre_commit_checklist.md # 신규 (백엔드 체크리스트)
│       └── testing_standards.md    # 신규 (백엔드 테스트 표준)
│
└── frontend/                        # 프론트엔드: React/TypeScript 특화
    └── .claude/
        ├── CLAUDE.md               # 기존 (프론트엔드 컨텍스트)
        ├── agent.md                # 신규 (프론트엔드 개발 규칙)
        ├── pre_commit_checklist.md # 신규 (프론트엔드 체크리스트)
        └── testing_standards.md    # 신규 (프론트엔드 테스트 표준)
```

### 역할 정의

1. **루트 레벨 (헌법)**: 모든 도메인에 공통으로 적용되는 핵심 원칙
   - `PRINCIPLES.md`: TDD, Small Commits, 점진적 개선 등 핵심 철학
   - `quality_standards.md`: 코드 품질, 에러 처리, 테스트 커버리지 기준
   - `decision_framework.md`: Trade-off 분석, ADR 작성 방법

2. **백엔드 레벨**: Java/Spring Boot 개발에 특화된 구체적 가이드
   - `agent.md`: 백엔드 개발 시 Claude가 따라야 할 규칙
   - `pre_commit_checklist.md`: 백엔드 커밋 전 검증 항목
   - `testing_standards.md`: Java 테스트 작성 표준

3. **프론트엔드 레벨**: React/TypeScript 개발에 특화된 구체적 가이드
   - `agent.md`: 프론트엔드 개발 시 Claude가 따라야 할 규칙
   - `pre_commit_checklist.md`: 프론트엔드 커밋 전 검증 항목
   - `testing_standards.md`: React 테스트 작성 표준

## 적용 대상 요소

### 1. 개발 방법론
- ✅ Test-Driven Development (TDD)
- ✅ Small, focused commits ("Tidy First" 원칙)
- ✅ 점진적/반복적 개선
- ✅ Pre-commit hygiene checklist

### 2. 문서화 관행
- ✅ Architecture Decision Records (ADR)
- ✅ AI 컨텍스트 강화 (.claude/system_prompt_additions.md)
- ✅ 분석 문서 템플릿 (test coverage, performance)

### 3. 품질 표준
- ✅ Comprehensive testing strategy (unit, integration, E2E)
- ✅ Static analysis integration
- ✅ Performance benchmarking
- ✅ Code quality gates

### 4. 의사결정 프레임워크
- ✅ Trade-off 기반 분석
- ✅ 구조화된 비교 평가
- ✅ 3단계 시간 전략 (즉시/중기/장기)

---

## 단계별 적용 계획

### Phase 1: 루트 레벨 헌법 문서 작성 (1 세션)

**목표**: 모든 도메인에 공통으로 적용되는 핵심 원칙 문서 작성

#### 작업 목록

1. **`.claude/PRINCIPLES.md` 작성**
   - **내용 출처**: BPlusTree3의 `agent.md` 중 공통 원칙 부분
   - **포함 내용**:
     - Test-Driven Development (TDD) 원칙
     - Small, focused commits ("Tidy First")
     - 점진적/반복적 개선 철학
     - Dead code 제거 규칙
     - 코드 리뷰 문화

2. **`.claude/quality_standards.md` 작성**
   - **내용 출처**: BPlusTree3의 `.claude/system_prompt_additions.md`
   - **포함 내용**:
     - 코드 품질 기준 (모든 언어 공통)
     - 에러 처리 규칙 (panic/throw 금지 원칙)
     - 테스트 커버리지 목표 (>80%)
     - 성능 기준 (API 응답 시간)
     - 메모리 관리 원칙
     - Pre-completion 검증 체크리스트

3. **`.claude/decision_framework.md` 작성**
   - **내용 출처**: BPlusTree3의 `arena_elimination_analysis.md` 패턴
   - **포함 내용**:
     - Trade-off 분석 프레임워크
     - 3단계 시간 전략 (즉시/중기/장기)
     - 의사결정 템플릿
     - ADR 작성 가이드

4. **`docs/adr/` 디렉토리 구조 생성**
   ```
   docs/
   ├── adr/
   │   ├── README.md          # ADR 작성 가이드
   │   ├── template.md        # ADR 템플릿
   │   └── 0001-example.md    # 예시 ADR
   └── analysis/              # 분석 문서 템플릿
       ├── test_coverage_template.md
       └── performance_analysis_template.md
   ```

**완료 기준**:
- [ ] `.claude/PRINCIPLES.md` 작성 완료
- [ ] `.claude/quality_standards.md` 작성 완료
- [ ] `.claude/decision_framework.md` 작성 완료
- [ ] `docs/adr/` 디렉토리 구조 생성
- [ ] `docs/analysis/` 템플릿 작성

**예상 시간**: 2-3시간

**BPlusTree3 참고 파일**:
- [agent.md](https://github.com/KentBeck/BPlusTree3/blob/main/agent.md)
- [.claude/system_prompt_additions.md](https://github.com/KentBeck/BPlusTree3/blob/main/.claude/system_prompt_additions.md)
- [arena_elimination_analysis.md](https://github.com/KentBeck/BPlusTree3/blob/main/arena_elimination_analysis.md)

---

### Phase 2: 백엔드 특화 문서 작성 (1 세션)

**목표**: Java/Spring Boot 개발에 특화된 Claude 가이드 작성

#### 작업 목록

1. **`backend/.claude/agent.md` 작성**
   - **내용 출처**: BPlusTree3의 `agent.md`를 Java/Spring Boot에 맞게 변환
   - **포함 내용**:
     - Java 개발 규칙 (naming conventions, package structure)
     - Spring Boot 베스트 프랙티스 (DI, REST API, JPA)
     - 에러 처리 규칙 (Exception hierarchy, @ControllerAdvice)
     - 성능 최적화 규칙 (N+1 문제 방지, caching)
     - 벤치마크 실행 규칙 (JMH 사용법)

2. **`backend/.claude/pre_commit_checklist.md` 작성**
   - **내용 출처**: BPlusTree3의 `agent.md` "Pre-Commit Hygiene Checklist" 섹션
   - **포함 내용**:
     - Dead code 제거 확인
     - Code formatting: `./gradlew spotlessApply`
     - Test 실행: `./gradlew test`
     - Static analysis: `./gradlew checkstyleMain`
     - Build 검증: `./gradlew build`

3. **`backend/.claude/testing_standards.md` 작성**
   - **내용 출처**: BPlusTree3의 `.claude/system_prompt_additions.md` 테스트 섹션
   - **포함 내용**:
     - Unit test 작성 표준 (JUnit 5, Mockito)
     - Integration test 작성 표준 (@SpringBootTest)
     - Test naming convention
     - Test coverage 목표 (>80%)
     - Property-based testing (jqwik) 가이드

**완료 기준**:
- [ ] `backend/.claude/agent.md` 작성 완료
- [ ] `backend/.claude/pre_commit_checklist.md` 작성 완료
- [ ] `backend/.claude/testing_standards.md` 작성 완료
- [ ] Claude가 백엔드 작업 시 문서를 참조하는지 확인

**예상 시간**: 2-3시간

**BPlusTree3 참고 파일**:
- [agent.md](https://github.com/KentBeck/BPlusTree3/blob/main/agent.md) - 개발 규칙
- [.claude/system_prompt_additions.md](https://github.com/KentBeck/BPlusTree3/blob/main/.claude/system_prompt_additions.md) - 품질 표준

---

### Phase 3: 프론트엔드 특화 문서 작성 (1 세션)

**목표**: React/TypeScript 개발에 특화된 Claude 가이드 작성

#### 작업 목록

1. **`frontend/.claude/agent.md` 작성**
   - **내용 출처**: BPlusTree3의 `agent.md`를 React/TypeScript에 맞게 변환
   - **포함 내용**:
     - React 개발 규칙 (Hooks, Component 구조)
     - TypeScript 베스트 프랙티스 (타입 정의, 제네릭)
     - 에러 처리 규칙 (Error Boundary, try-catch)
     - 성능 최적화 규칙 (useMemo, useCallback, React.memo)
     - 접근성 표준 (ARIA, 키보드 네비게이션)

2. **`frontend/.claude/pre_commit_checklist.md` 작성**
   - **내용 출처**: BPlusTree3의 `agent.md` "Pre-Commit Hygiene Checklist" 섹션
   - **포함 내용**:
     - Dead code 제거 확인
     - Code formatting: `npm run format`
     - Type checking: `npm run type-check`
     - Linting: `npm run lint`
     - Test 실행: `npm test`
     - Build 검증: `npm run build`

3. **`frontend/.claude/testing_standards.md` 작성**
   - **내용 출처**: BPlusTree3의 `.claude/system_prompt_additions.md` 테스트 섹션
   - **포함 내용**:
     - Unit test 작성 표준 (Vitest/Jest, React Testing Library)
     - Component test 작성 표준
     - E2E test 작성 표준 (Playwright)
     - Test naming convention
     - Test coverage 목표 (>80%)

**완료 기준**:
- [ ] `frontend/.claude/agent.md` 작성 완료
- [ ] `frontend/.claude/pre_commit_checklist.md` 작성 완료
- [ ] `frontend/.claude/testing_standards.md` 작성 완료
- [ ] Claude가 프론트엔드 작업 시 문서를 참조하는지 확인

**예상 시간**: 2-3시간

**BPlusTree3 참고 파일**:
- [agent.md](https://github.com/KentBeck/BPlusTree3/blob/main/agent.md) - 개발 규칙
- [.claude/system_prompt_additions.md](https://github.com/KentBeck/BPlusTree3/blob/main/.claude/system_prompt_additions.md) - 품질 표준

---

### Phase 4: Pre-commit Hook 자동화 (1 세션)

**목표**: 백엔드/프론트엔드 각각 pre-commit hook 스크립트 작성 및 설치

#### 작업 목록

1. **백엔드 Pre-commit Hook 작성**
   - `backend/scripts/pre-commit.sh` 작성
   - `backend/.claude/pre_commit_checklist.md`의 항목들을 자동화
   - Spotless, Checkstyle, Test 실행

2. **프론트엔드 Pre-commit Hook 작성**
   - `frontend/scripts/pre-commit.sh` 작성
   - `frontend/.claude/pre_commit_checklist.md`의 항목들을 자동화
   - Prettier, ESLint, Type checking, Test 실행

3. **Git Hook 설치 자동화**
   - `scripts/install-hooks.sh` 작성 (루트 레벨)
   - 백엔드/프론트엔드 hook을 각각 `.git/hooks/pre-commit`에 설치

**완료 기준**:
- [ ] `backend/scripts/pre-commit.sh` 작성 및 테스트
- [ ] `frontend/scripts/pre-commit.sh` 작성 및 테스트
- [ ] `scripts/install-hooks.sh` 작성
- [ ] README에 hook 설치 가이드 추가

**예상 시간**: 2-3시간

---

### Phase 5: 실제 적용 및 검증 (지속적)

**목표**: 작성한 문서들을 실제 개발에 적용하고 효과 검증

#### 작업 목록

1. **첫 번째 ADR 작성**
   - 현재 진행 중인 기술적 결정 선택 (예: A/B 테스트 구현 방식)
   - `.claude/decision_framework.md` 템플릿 적용
   - Trade-off 분석 포함

2. **테스트 커버리지 측정**
   - JaCoCo로 백엔드 커버리지 측정
   - 목표: >80% 달성
   - `docs/analysis/test_coverage_report.md` 작성

3. **성능 벤치마크 설정 (선택사항)**
   - JMH로 백엔드 핵심 경로 벤치마크 작성
   - 성능 기준선 설정

4. **문서 효과 검증**
   - Claude가 `.claude/` 문서들을 실제로 참조하는지 확인
   - 코드 리뷰 시 품질 표준 준수 여부 확인

**완료 기준**:
- [ ] ADR 1개 이상 작성
- [ ] 테스트 커버리지 >80% 달성
- [ ] Claude가 문서를 실제로 활용하는 것을 확인

**예상 시간**: 지속적 (매주 1-2시간)

---

## 세션별 진행 순서 제안

### 세션 1: 루트 레벨 헌법 작성 (Phase 1)
- `.claude/PRINCIPLES.md` 작성
- `.claude/quality_standards.md` 작성
- `.claude/decision_framework.md` 작성
- `docs/adr/`, `docs/analysis/` 디렉토리 구조 생성

**우선순위**: 🔴 HIGH (모든 도메인의 기반)

### 세션 2: 백엔드 특화 문서 작성 (Phase 2)
- `backend/.claude/agent.md` 작성
- `backend/.claude/pre_commit_checklist.md` 작성
- `backend/.claude/testing_standards.md` 작성

**우선순위**: 🟡 MEDIUM (백엔드 개발 중이면 HIGH)

### 세션 3: 프론트엔드 특화 문서 작성 (Phase 3)
- `frontend/.claude/agent.md` 작성
- `frontend/.claude/pre_commit_checklist.md` 작성
- `frontend/.claude/testing_standards.md` 작성

**우선순위**: 🟡 MEDIUM (프론트엔드 개발 중이면 HIGH)

### 세션 4: Pre-commit Hook 자동화 (Phase 4)
- `backend/scripts/pre-commit.sh` 작성
- `frontend/scripts/pre-commit.sh` 작성
- `scripts/install-hooks.sh` 작성

**우선순위**: 🟢 LOW (여유 있을 때)

### 세션 5+: 실제 적용 및 검증 (Phase 5)
- ADR 작성 실습
- 테스트 커버리지 측정 및 개선
- 문서 효과 검증

**우선순위**: 🔵 CONTINUOUS (지속적 실천)

---

## 성공 지표

### 단기 (1주)
- [ ] **Phase 1 완료**: 루트 레벨 헌법 문서 3개 작성
  - `.claude/PRINCIPLES.md`
  - `.claude/quality_standards.md`
  - `.claude/decision_framework.md`
- [ ] **Phase 2 완료**: 백엔드 특화 문서 3개 작성
  - `backend/.claude/agent.md`
  - `backend/.claude/pre_commit_checklist.md`
  - `backend/.claude/testing_standards.md`

### 중기 (2-4주)
- [ ] **Phase 3 완료**: 프론트엔드 특화 문서 3개 작성
- [ ] **Phase 4 완료**: Pre-commit hook 자동화
- [ ] Claude가 새 문서들을 실제로 참조하는 것 확인

### 장기 (1-3개월)
- [ ] ADR 3개 이상 작성 (실제 의사결정 문서화)
- [ ] 테스트 커버리지 >80% 달성
- [ ] Small commits 문화 정착 (평균 커밋 크기 감소)
- [ ] Trade-off 기반 의사결정이 팀 표준으로 정착

---

## 주의사항

### DO
- ✅ 단계적으로 적용 (한 번에 하나씩)
- ✅ 팀원들과 합의 후 진행
- ✅ 기존 워크플로우 존중
- ✅ 문서화 우선
- ✅ 자동화 최대한 활용

### DO NOT
- ❌ 한 번에 모든 것을 적용하려고 하지 말 것
- ❌ 팀원들의 반대를 무시하지 말 것
- ❌ 기존 프로세스를 급격히 변경하지 말 것
- ❌ 문서화 없이 프로세스 변경하지 말 것
- ❌ 자동화 없이 수동 프로세스만 의존하지 말 것

---

## 참고 자료

### BPlusTree3 핵심 문서
- [agent.md](https://github.com/KentBeck/BPlusTree3/blob/main/agent.md) - 개발 규칙 및 체크리스트
- [.claude/system_prompt_additions.md](https://github.com/KentBeck/BPlusTree3/blob/main/.claude/system_prompt_additions.md) - 품질 표준
- [arena_elimination_analysis.md](https://github.com/KentBeck/BPlusTree3/blob/main/arena_elimination_analysis.md) - Trade-off 분석 예시
- [test_coverage_analysis.md](https://github.com/KentBeck/BPlusTree3/blob/main/test_coverage_analysis.md) - 테스트 전략 예시

### 관련 개념
- **Tidy First**: Kent Beck의 리팩토링 방법론 (작은 개선을 먼저)
- **Test-Driven Development**: 테스트 먼저 작성 후 구현
- **Architecture Decision Records**: 아키텍처 결정 문서화
- **Property-Based Testing**: 속성 기반 테스트

---

## 다음 단계

### 즉시 시작: Phase 1 (루트 레벨 헌법 작성)

다음 세션에서 진행할 작업:

1. **BPlusTree3 파일 다운로드 및 참고**
   - [agent.md](https://github.com/KentBeck/BPlusTree3/blob/main/agent.md)
   - [.claude/system_prompt_additions.md](https://github.com/KentBeck/BPlusTree3/blob/main/.claude/system_prompt_additions.md)
   - [arena_elimination_analysis.md](https://github.com/KentBeck/BPlusTree3/blob/main/arena_elimination_analysis.md)

2. **루트 레벨 문서 작성**
   ```bash
   # Claude에게 요청:
   # "Phase 1을 진행하자. BPlusTree3의 agent.md를 참고해서
   #  .claude/PRINCIPLES.md를 작성해줘."
   ```

3. **디렉토리 구조 생성**
   ```bash
   mkdir -p docs/adr docs/analysis
   ```

### 권장 순서

**백엔드 개발 중이라면**:
- Phase 1 → Phase 2 (백엔드 특화) → Phase 4 (Hook 자동화)

**프론트엔드 개발 중이라면**:
- Phase 1 → Phase 3 (프론트엔드 특화) → Phase 4 (Hook 자동화)

**둘 다 진행 중이라면**:
- Phase 1 → Phase 2 → Phase 3 → Phase 4

### 질문사항

- 어떤 Phase부터 시작하고 싶으신가요?
- 백엔드 개발 중인가요, 프론트엔드 개발 중인가요?
- 팀 상황에 맞게 조정이 필요한 부분이 있나요?
