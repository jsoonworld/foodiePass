# FoodiePass - 다음 세션 작업 목록

**현재 상태**: feature/local-integration-test 브랜치 (커밋 완료)
**최신 커밋**: `2fe6d53` - API 키 환경변수 처리 및 .env 설정 완료

---

## ✅ 완료된 작업

1. API 키 보안 처리
   - application-local.yml에서 하드코딩된 API 키 제거
   - `backend/.env.example` 템플릿 파일 생성
   - `backend/.env` 파일에 실제 키값 설정 (gitignore됨)

2. 코드 개선
   - `name()` → `getLanguageName()` 메서드 사용 (API 명확성 개선)
   - GeminiTranslationClient, GeminiOcrReader, TasteAtlasFoodScrapper 업데이트

---

## 🔴 긴급 (다음 세션 최우선)

### 1. develop 브랜치로 이동 및 검증
```bash
# 현재 feature 브랜치 푸시
git push origin feature/local-integration-test

# develop 브랜치로 이동
git checkout develop
git pull origin develop

# 최신 상태 확인
git log --oneline -10
git status
```

### 2. 프로젝트 동작 검증
```bash
# 백엔드 실행 (새 터미널)
cd backend
export SPOONACULAR_API_KEY="1fe91ac5a2614fe985481f65a21ce6f6"
./gradlew bootRun --args='--spring.profiles.active=local' -Dorg.gradle.jvmargs='-Xmx4g -Xms1g'

# 프론트엔드 실행 (다른 터미널)
cd frontend
npm install
npm run dev

# API 테스트
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/admin/ab-test/results
curl http://localhost:8080/api/admin/surveys/results
```

---

## 🟡 중요 (오늘 중 처리)

### 1. 백그라운드 프로세스 정리
```bash
# 실행 중인 모든 Gradle 프로세스 종료
ps aux | grep gradle | grep -v grep | awk '{print $2}' | xargs kill -9

# 실행 중인 npm dev 서버 종료
lsof -ti:3000 | xargs kill -9
lsof -ti:8080 | xargs kill -9

# 임시 로그 파일 삭제
rm -f /tmp/backend-*.log
rm -f /tmp/frontend-*.log
```

### 2. Git Worktree 정리 (선택)
```bash
# Worktree 목록 확인
git worktree list

# 사용하지 않는 worktree 제거 (필요시)
# git worktree remove /path/to/worktree
```

### 3. 머지된 브랜치 정리
```bash
# develop에 이미 머지된 로컬 브랜치 확인
git branch --merged develop

# 안전하게 삭제 가능한 브랜치 (확인 후)
# git branch -d feature/local-integration-test  # develop 머지 후에만
```

---

## 🟢 권장 (시간 여유 있을 때)

### 1. 문서 업데이트

#### A. 현재 상태 문서 작성
**파일**: `docs/CURRENT_STATUS.md`
```markdown
# FoodiePass MVP - 현재 상태

## 구현 완료
- A/B 테스트 시스템 (ABTestService)
- 설문 시스템 (SurveyService)
- 메뉴 스캔 API (Control/Treatment 분기)
- 프론트엔드 UI (Control/Treatment)

## 다음 단계
- E2E 테스트
- 스테이징 환경 배포
- 실제 사용자 테스트 (20명)
```

#### B. 로컬 실행 가이드 업데이트
**파일**: `README.md` (루트)
```bash
# 환경변수 설정 추가
cp backend/.env.example backend/.env
# .env 파일 수정 (실제 API 키 입력)
```

#### C. 배포 가이드 초안
**파일**: `docs/DEPLOYMENT.md`
```markdown
# FoodiePass 배포 가이드

## 필요한 환경변수
- SPOONACULAR_API_KEY
- GEMINI_API_KEY
- Google Cloud 서비스 계정 JSON 파일

## 인프라 요구사항
- AWS EC2 (백엔드)
- AWS RDS (MySQL)
- AWS S3 (프론트엔드 정적 호스팅)
- Vercel (대안)
```

### 2. 프로젝트 정리

#### A. 불필요한 파일 삭제
```bash
# 빌드 아티팩트 정리
cd backend && ./gradlew clean
cd frontend && rm -rf .next

# node_modules 재설치 (선택)
cd frontend && rm -rf node_modules && npm install
```

#### B. 테스트 실행
```bash
# 백엔드 테스트
cd backend && ./gradlew test

# 프론트엔드 테스트 (있다면)
cd frontend && npm run test
```

---

## 📝 다음 단계 계획

### Phase 4: E2E 테스트 및 배포 (예정)

#### 1주차
- [ ] Playwright E2E 테스트 작성
- [ ] Control/Treatment UI 자동 검증
- [ ] A/B 테스트 플로우 자동화

#### 2주차
- [ ] AWS 인프라 설정 (EC2, RDS, S3)
- [ ] 환경변수 설정
- [ ] 스테이징 환경 배포

#### 3주차
- [ ] 실제 사용자 테스트 (20명)
- [ ] 가설 검증 데이터 수집
- [ ] H2, H3 결과 분석

---

## 🚨 주의사항

### API 키 보안
- **절대 커밋하지 말 것**: `backend/.env` 파일
- **커밋해야 할 것**: `backend/.env.example` 파일 (템플릿만)
- `.gitignore`에 `.env` 패턴 확인: ✅ 완료

### 환경변수 설정
```bash
# 로컬 실행 시 필수
export SPOONACULAR_API_KEY="1fe91ac5a2614fe985481f65a21ce6f6"
export GEMINI_API_KEY="your-gemini-api-key"

# 또는 backend/.env 파일에 설정
```

### Git Workflow
```bash
# feature 브랜치에서 작업
git checkout feature/YOUR_FEATURE

# 커밋 후 develop으로 PR
git push origin feature/YOUR_FEATURE
# GitHub에서 PR 생성

# develop 브랜치는 직접 푸시 금지
```

---

## 📊 현재 상태 요약

### 구현 현황
- ✅ 백엔드: ABTest, Survey 모듈 완료
- ✅ 프론트엔드: Control/Treatment UI 완료
- ✅ 통합: 메뉴 스캔 API 완료
- ⏳ 배포: 준비 단계

### 가설 검증 준비도
- H1 (핵심 가치): 80% (설문 시스템 완료, 실제 데이터 필요)
- H2 (기술 실현): 70% (기능 완료, 성능 검증 필요)
- H3 (사용자 행동): 60% (시스템 완료, 사용자 테스트 필요)

---

## 🔗 유용한 명령어 모음

### 개발 서버 실행
```bash
# 백엔드
cd backend && ./gradlew bootRun --args='--spring.profiles.active=local'

# 프론트엔드
cd frontend && npm run dev
```

### Git 명령어
```bash
# 브랜치 확인
git branch -vv

# 최근 커밋 확인
git log --oneline -10

# 변경사항 확인
git status
git diff

# 스테이징 상태 확인
git diff --cached
```

### 디버깅
```bash
# 실행 중인 프로세스 확인
ps aux | grep java
ps aux | grep node

# 포트 사용 확인
lsof -i:8080
lsof -i:3000

# 로그 확인
tail -f /tmp/backend-*.log
```

---

## ✅ 체크리스트 (다음 세션 시작 시)

- [ ] feature/local-integration-test 브랜치 푸시 완료
- [ ] develop 브랜치로 이동
- [ ] 백엔드 서버 정상 실행 확인
- [ ] 프론트엔드 서버 정상 실행 확인
- [ ] API 엔드포인트 동작 확인
- [ ] A/B 테스트 동작 확인
- [ ] 설문 시스템 동작 확인
- [ ] 백그라운드 프로세스 정리
- [ ] 문서 업데이트 (선택)

---

**마지막 업데이트**: 2025-11-09 11:15
**작성자**: Claude Code Assistant
**다음 세션 예상 소요 시간**: 2-3시간
