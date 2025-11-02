# FoodiePass Frontend

Web application for FoodiePass - helping travelers understand foreign menus.

## Tech Stack

- **Framework**: TBD (React/Next.js)
- **Language**: TypeScript
- **State Management**: TBD
- **Styling**: TBD
- **Build Tool**: TBD (Vite/Next.js)

## Prerequisites

- Node.js 18+ or higher
- npm/yarn/pnpm

## Getting Started

> ⚠️ **Note**: Frontend setup is in progress. Documentation will be updated once the framework is decided.

### Planned Setup

```bash
# Install dependencies
npm install

# Run development server
npm run dev

# Build for production
npm run build

# Run tests
npm test
```

## Project Structure (Planned)

```
frontend/
├── src/
│   ├── components/      # Reusable UI components
│   ├── pages/          # Page components
│   ├── services/       # API client services
│   ├── hooks/          # Custom React hooks
│   ├── utils/          # Utility functions
│   ├── assets/         # Static assets
│   └── App.tsx         # Main app component
├── public/             # Public static files
├── package.json
└── tsconfig.json
```

## Core Features to Implement

### 1. Menu Scanning Flow
- Camera/file upload interface
- OCR processing feedback
- Menu display with translations

### 2. Menu Browsing
- Visual menu grid with food photos
- Item details modal
- Allergen and ingredient information

### 3. Cart & Ordering
- Add/remove items
- Quantity adjustment
- Order summary

### 4. Checkout
- Bilingual order script generation
- Display for user and restaurant staff

## API Integration

The frontend communicates with the backend API. See [API Contract](../docs/API_CONTRACT.md) _(TBD)_ for endpoint specifications.

### API Client Setup (Planned)

```typescript
// services/api.ts
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

// API client implementation
```

## Development Guidelines

### Component Organization
- One component per file
- Use functional components with hooks
- Implement proper TypeScript types

### State Management
- TBD (Context API / Redux / Zustand)

### Styling Approach
- TBD (Tailwind / CSS Modules / Styled Components)

### Testing
- Unit tests for utilities and hooks
- Component tests with React Testing Library
- E2E tests for critical flows

## Environment Variables

Create `.env.local` for development:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
# Add other environment variables
```

## Related Documentation

- [Project README](../README.md)
- [Technical Specification](../docs/TECH_SPEC.md)
- [API Contract](../docs/API_CONTRACT.md) _(TBD)_
