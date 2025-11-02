# FoodiePass v2 - Context for Claude

## Project Status

- **Phase**: MVP Refactoring from existing v1
- **Branch**: `develop` (main development branch)
- **Current Focus**: Awaiting 1-Pager and PRD from user

## Repository Structure

```
foodiePass/
├── backend/          # Spring Boot 3.5.3, Java 21
├── frontend/         # TBD (React/Next.js)
├── docs/            # Project documentation
├── scripts/         # Development scripts
└── .claude/         # AI assistant context
```

## Quick Links

- [Project README](../README.md) - Monorepo overview
- [Backend README](../backend/README.md) - Backend setup
- [Frontend README](../frontend/README.md) - Frontend setup (TBD)

### Documentation (To be created)
- [1-Pager](../docs/1-PAGER.md) - Project vision and goals
- [PRD](../docs/PRD.md) - Product requirements
- [Tech Spec](../docs/TECH_SPEC.md) - Technical architecture
- [API Contract](../docs/API_CONTRACT.md) - Frontend-Backend API specification

## Working Principles

### 1. MVP First
- Build only what's defined in scope documents
- No feature creep beyond current iteration
- Complete features fully before moving to next

### 2. Code Organization
- **Backend**: Follow existing layered architecture (api → application → domain → infra)
- **Frontend**: TBD based on framework choice
- **Tests**: Maintain >80% coverage for new code

### 3. Development Workflow
```bash
# Backend development
cd backend
./gradlew bootRun

# Frontend development (once set up)
cd frontend
npm run dev
```

## Tech Stack

### Backend (Existing)
- **Framework**: Spring Boot 3.5.3
- **Language**: Java 21
- **Database**: H2 (dev), PostgreSQL (prod planned)
- **External APIs**:
  - Google Cloud Vertex AI (OCR, AI)
  - Google Cloud Translation
  - TasteAtlas (food images)
  - Currency API
- **Tools**: Gradle, Lombok, JUnit 5

### Frontend (To be decided)
- **Framework**: TBD (React/Next.js)
- **Language**: TypeScript
- **Styling**: TBD
- **State Management**: TBD

## Domain Model Overview

### Core Domains
1. **Menu**: OCR extraction, enrichment, food image scraping
2. **Language**: Translation services
3. **Currency**: Exchange rate conversion
4. **Order**: Cart and order management
5. **Script**: Bilingual order script generation

### Key External Integrations
- Google Cloud (Vertex AI, Translation)
- Selenium (web scraping)
- TasteAtlas (food database)

## Current Iteration: [Awaiting Definition]

**Goal**: [To be defined after receiving 1-Pager and PRD]

**Scope**: [To be defined]

**Success Criteria**: [To be defined]

## Do NOT

- ❌ Add features beyond defined MVP scope
- ❌ Change database schema without discussion
- ❌ Create infrastructure/deployment configs (not in current scope)
- ❌ Modify working v1 functionality without clear reason
- ❌ Use marketing language or superlatives in code/comments

## DO

- ✅ Follow existing code patterns in backend
- ✅ Write tests for all new functionality
- ✅ Ask for clarification when requirements are unclear
- ✅ Use git feature branches for all work
- ✅ Keep commits small and focused
- ✅ Update documentation when making significant changes

## Next Steps

1. ⏳ **Awaiting user documents**: 1-Pager and PRD
2. ⏳ **Create docs/**: Populate with user's documents
3. ⏳ **Define current iteration**: Based on documents
4. ⏳ **Choose frontend framework**: React vs Next.js
5. ⏳ **Create API contract**: Define frontend-backend interface

## Notes for Claude

- This project has existing v1 implementation in backend/
- User is restructuring for v2 with frontend separation
- Focus on incremental improvements, not complete rewrites
- Maintain existing external API integrations
- User will provide direction via 1-Pager and PRD documents
