# Backend Technical Documentation

백엔드 상세 기술 문서입니다.

## 문서 구조

- **API.md**: REST API 명세서
- **DATABASE.md**: 데이터베이스 스키마 및 ERD
- **IMPLEMENTATION.md**: 구현 계획 및 진행 상황

## 프로젝트 전체 문서

백엔드 개발 시 다음 문서를 참조하세요:

- [1-PAGER](../../docs/1-PAGER.md): 프로젝트 비전, 핵심 가설
- [PRD](../../docs/PRD.md): 요구사항 명세
- [TECH_SPEC](../../docs/TECH_SPEC.md): 전체 시스템 아키텍처

## 개발 환경 설정

### 실행
```bash
cd backend
./gradlew bootRun --args='--spring.profiles.active=local'
```bash

### 테스트
```bash
./gradlew test
```bash

### 빌드
```bash
./gradlew clean build
```text
