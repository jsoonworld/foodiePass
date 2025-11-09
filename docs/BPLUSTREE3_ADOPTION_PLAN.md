# BPlusTree3 방법론 적용 계획서

## 개요

Kent Beck의 BPlusTree3 프로젝트에서 FoodiePass에 적용할 개발 방법론 및 모범 사례를 단계적으로 도입하기 위한 계획입니다.

**Source**: https://github.com/KentBeck/BPlusTree3

**목표**: B+ Tree 데이터 구조가 아닌, Kent Beck의 **개발 방법론, 품질 표준, 문서화 관행**을 FoodiePass 프로젝트에 적용

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

### Phase 1: 문서 인프라 구축 (1-2 세션)

**목표**: ADR 및 분석 문서 구조를 프로젝트에 도입

#### 작업 목록

1. **ADR 디렉토리 구조 생성**
   ```
   docs/
   ├── adr/
   │   ├── README.md          # ADR 작성 가이드
   │   ├── template.md        # ADR 템플릿
   │   └── 0001-example.md    # 예시 ADR
   ```

2. **ADR 작성 가이드 문서화**
   - ADR 작성 시점 (언제 ADR을 작성하는가?)
   - ADR 템플릿 (제목, 상태, 컨텍스트, 결정, 결과, 트레이드오프)
   - 기존 ADR 참조 방법

3. **분석 문서 템플릿 생성**
   ```
   docs/
   ├── analysis/
   │   ├── test_coverage_template.md
   │   ├── performance_analysis_template.md
   │   └── technical_debt_template.md
   ```

**완료 기준**:
- [ ] `docs/adr/` 디렉토리 생성 및 템플릿 작성
- [ ] `docs/analysis/` 디렉토리 생성 및 템플릿 3개 작성
- [ ] 팀원들이 ADR 작성 가이드를 읽고 이해

**예상 시간**: 1-2시간

---

### Phase 2: AI 컨텍스트 강화 (1 세션)

**목표**: Claude가 프로젝트 특화 품질 표준을 이해하도록 컨텍스트 강화

#### 작업 목록

1. **프로젝트 품질 표준 문서 작성**
   ```
   .claude/
   ├── CLAUDE.md (기존)
   ├── quality_standards.md (신규)
   └── commit_conventions.md (신규)
   ```

2. **quality_standards.md 작성**
   - Java/Spring Boot 특화 품질 표준
   - 에러 처리 규칙 (Result-like patterns, exception hierarchy)
   - 테스트 커버리지 목표 (>80%)
   - 성능 기준 (API 응답 시간 ≤ 5초)
   - Dead code 제거 규칙

3. **commit_conventions.md 작성**
   - Commit message 규칙 (Conventional Commits)
   - Small, focused commits 가이드
   - Pre-commit checklist
   - Branch naming convention

**완료 기준**:
- [ ] `.claude/quality_standards.md` 작성 완료
- [ ] `.claude/commit_conventions.md` 작성 완료
- [ ] Claude가 새 문서를 참조하여 코드 리뷰 가능 확인

**예상 시간**: 1-2시간

---

### Phase 3: Pre-Commit Hygiene 도입 (1 세션)

**목표**: 커밋 전 자동화된 품질 검증 체계 구축

#### 작업 목록

1. **Pre-commit hook 스크립트 작성**
   ```
   scripts/
   ├── pre-commit.sh
   └── quality-check.sh
   ```

2. **quality-check.sh 구현**
   - Static analysis: Checkstyle, SpotBugs
   - Code formatting: Google Java Format
   - Test execution: `./gradlew test`
   - Build verification: `./gradlew build`

3. **Git hook 설치 자동화**
   - `scripts/install-hooks.sh` 스크립트 작성
   - `.git/hooks/pre-commit` 자동 설치
   - README에 설치 가이드 추가

4. **CI/CD 통합**
   - GitHub Actions에 동일한 품질 검증 추가
   - PR 시 자동 실행

**완료 기준**:
- [ ] `scripts/pre-commit.sh` 작성 및 테스트
- [ ] `scripts/quality-check.sh` 작성 및 테스트
- [ ] Git hook 자동 설치 스크립트 작성
- [ ] CI/CD 파이프라인에 품질 검증 추가
- [ ] README에 설치 가이드 추가

**예상 시간**: 2-3시간

---

### Phase 4: 테스트 전략 재정의 (1-2 세션)

**목표**: BPlusTree3의 계층적 테스트 전략을 FoodiePass에 적용

#### 작업 목록

1. **테스트 분류 체계 정의**
   - Fast CI Tests: 단위 테스트, 통합 테스트 (빠른 피드백)
   - Reliability Tests: E2E 테스트, 성능 테스트 (느리지만 중요)
   - 각 분류별 실행 시점 정의

2. **테스트 커버리지 분석**
   - 현재 테스트 커버리지 측정 (JaCoCo)
   - 미커버 영역 식별 및 우선순위 설정
   - `docs/analysis/test_coverage_analysis.md` 작성

3. **테스트 작성 가이드 문서화**
   ```
   docs/
   ├── testing_strategy.md
   └── test_writing_guide.md
   ```

4. **Property-based testing 도입 검토**
   - Java용 property-based testing 라이브러리 조사 (jqwik)
   - 핵심 비즈니스 로직에 적용 (OCR 파이프라인 등)

**완료 기준**:
- [ ] 테스트 분류 체계 문서화
- [ ] 현재 커버리지 분석 완료
- [ ] `docs/testing_strategy.md` 작성
- [ ] Property-based testing 적용 가능성 평가

**예상 시간**: 3-4시간

---

### Phase 5: 성능 벤치마킹 체계 구축 (1 세션)

**목표**: 성능 회귀를 방지하기 위한 벤치마킹 체계 도입

#### 작업 목록

1. **JMH 벤치마크 설정**
   - JMH (Java Microbenchmark Harness) 의존성 추가
   - 벤치마크 모듈 구조 생성 (`backend/src/jmh/`)

2. **핵심 경로 벤치마크 작성**
   - OCR 파이프라인 처리 시간
   - 음식 매칭 API 응답 시간
   - 환율 변환 처리 시간
   - End-to-end 메뉴 스캔 시간

3. **성능 기준선 설정**
   - 현재 성능 측정 및 기준선 설정
   - `docs/analysis/performance_baseline.md` 작성

4. **CI/CD 통합**
   - 성능 회귀 감지 자동화
   - PR 시 벤치마크 실행 및 비교

**완료 기준**:
- [ ] JMH 설정 완료
- [ ] 핵심 경로 벤치마크 4개 이상 작성
- [ ] 성능 기준선 문서화
- [ ] CI/CD에 벤치마크 통합

**예상 시간**: 2-3시간

---

### Phase 6: 의사결정 프레임워크 적용 (지속적)

**목표**: 아키텍처 결정 시 BPlusTree3의 trade-off 기반 분석 방법론 적용

#### 작업 목록

1. **의사결정 템플릿 작성**
   ```
   docs/
   ├── decision_framework.md
   └── adr/
       └── template.md (확장)
   ```

2. **Trade-off 분석 체크리스트**
   - 성능 vs 복잡성
   - 단기 vs 장기 이점
   - 안전성 vs 개발 속도
   - 비용 vs 확장성

3. **3단계 시간 전략 템플릿**
   - 즉시 조치 (Immediate)
   - 중기 계획 (Medium-term)
   - 장기 비전 (Long-term)

4. **첫 번째 ADR 작성 (실습)**
   - 현재 진행 중인 기술적 결정 선택
   - ADR 템플릿 적용하여 작성
   - Trade-off 분석 포함

**완료 기준**:
- [ ] `docs/decision_framework.md` 작성
- [ ] Trade-off 분석 체크리스트 작성
- [ ] ADR 템플릿 확장 (trade-off 섹션 추가)
- [ ] 첫 번째 실제 ADR 작성 (예: A/B 테스트 구현 방식)

**예상 시간**: 1-2시간 (초기 설정) + 지속적 적용

---

### Phase 7: 점진적 개선 문화 정착 (지속적)

**목표**: "Tidy First" 원칙을 팀 문화로 정착

#### 작업 목록

1. **Small commits 가이드라인**
   - 커밋 크기 권장사항 (변경 파일 수, 라인 수)
   - 논리적 단위로 커밋 분리하는 방법
   - Commit message 예시 모음

2. **Refactoring 전략 문서화**
   - 언제 리팩토링을 해야 하는가?
   - 리팩토링 전 테스트 작성 필수
   - 리팩토링과 기능 추가 분리

3. **Dead code 제거 자동화**
   - Static analysis tool 설정 (SpotBugs unused code detection)
   - CI에서 dead code 경고 표시
   - Dead code 정기 리뷰 일정 설정 (월 1회)

4. **Code review 체크리스트**
   - BPlusTree3 품질 표준 반영
   - Small commits 준수 여부
   - 테스트 커버리지 확인
   - Performance impact 평가

**완료 기준**:
- [ ] `docs/small_commits_guide.md` 작성
- [ ] `docs/refactoring_strategy.md` 작성
- [ ] Dead code 검출 자동화 설정
- [ ] Code review 체크리스트 작성 및 공유

**예상 시간**: 2-3시간 (초기 설정) + 지속적 실천

---

## 세션별 진행 순서 제안

### 세션 1: 문서 인프라 (Phase 1)
- ADR 디렉토리 구조 생성
- ADR 템플릿 및 가이드 작성
- 분석 문서 템플릿 작성

### 세션 2: AI 컨텍스트 (Phase 2)
- quality_standards.md 작성
- commit_conventions.md 작성
- Claude 테스트

### 세션 3: Pre-commit Hook (Phase 3)
- quality-check.sh 스크립트 작성
- Git hook 설치 자동화
- CI/CD 통합

### 세션 4: 테스트 전략 (Phase 4)
- 테스트 분류 체계 정의
- 테스트 커버리지 분석
- Testing strategy 문서 작성

### 세션 5: 성능 벤치마킹 (Phase 5)
- JMH 설정 및 벤치마크 작성
- 성능 기준선 설정
- CI/CD 통합

### 세션 6: 의사결정 프레임워크 (Phase 6)
- 의사결정 템플릿 작성
- Trade-off 분석 체크리스트
- 첫 번째 ADR 작성 실습

### 세션 7: 점진적 개선 문화 (Phase 7)
- Small commits 가이드라인
- Refactoring 전략 문서화
- Code review 체크리스트

---

## 성공 지표

### 단기 (1-2주)
- [ ] ADR 디렉토리 생성 및 템플릿 작성
- [ ] AI 컨텍스트 강화 (quality_standards.md, commit_conventions.md)
- [ ] Pre-commit hook 설치 및 CI/CD 통합

### 중기 (1개월)
- [ ] 테스트 커버리지 >80% 달성
- [ ] 성능 벤치마크 4개 이상 작성 및 기준선 설정
- [ ] ADR 3개 이상 작성 (실제 의사결정 문서화)
- [ ] 모든 PR에 pre-commit check 통과

### 장기 (2-3개월)
- [ ] Small commits 문화 정착 (평균 커밋 크기 감소)
- [ ] Dead code 제로 유지
- [ ] 성능 회귀 제로 (CI에서 자동 감지)
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

**즉시 시작**: Phase 1 (문서 인프라 구축)
```bash
# 다음 세션에서 실행
mkdir -p docs/adr docs/analysis
# ADR 템플릿 및 가이드 작성
```

**질문사항**:
- 어떤 Phase부터 시작하고 싶으신가요?
- 특정 요소에 대해 더 자세히 알고 싶으신가요?
- 팀 상황에 맞게 조정이 필요한 부분이 있나요?
