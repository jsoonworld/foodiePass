# Monitoring Setup Guide

## Overview

Monitoring is essential for tracking errors, performance, and user behavior in production.

## Sentry Error Monitoring

### Why Sentry?

- **Real-time error tracking**: Catch errors before users report them
- **Stack traces**: Detailed error information for debugging
- **Release tracking**: Link errors to specific deployments
- **Performance monitoring**: Track slow transactions
- **Free tier**: 5,000 events/month (sufficient for MVP)

### Setup Steps

#### 1. Create Sentry Account

1. Go to [sentry.io](https://sentry.io/)
2. Sign up for free account
3. Create new project
4. Select "React" as platform

#### 2. Install Sentry SDK

```bash
cd frontend
npm install @sentry/react @sentry/vite-plugin
```

#### 3. Configure Vite Plugin

Edit `vite.config.ts`:

```typescript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react-swc'
import path from 'path'
import { sentryVitePlugin } from '@sentry/vite-plugin'

export default defineConfig({
  plugins: [
    react(),
    // Sentry plugin for source map upload
    sentryVitePlugin({
      org: process.env.SENTRY_ORG,
      project: process.env.SENTRY_PROJECT,
      authToken: process.env.SENTRY_AUTH_TOKEN,
    }),
  ],
  // ... rest of config
})
```

#### 4. Initialize Sentry

Create `src/lib/sentry.ts`:

```typescript
import * as Sentry from "@sentry/react";

export function initSentry() {
  if (import.meta.env.PROD && import.meta.env.SENTRY_DSN) {
    Sentry.init({
      dsn: import.meta.env.SENTRY_DSN,
      environment: import.meta.env.MODE,

      // Performance Monitoring
      integrations: [
        Sentry.browserTracingIntegration(),
        Sentry.replayIntegration(),
      ],

      // Performance Monitoring sample rate
      tracesSampleRate: 1.0, // 100% for MVP, reduce in production

      // Session Replay sample rate
      replaysSessionSampleRate: 0.1, // 10% of sessions
      replaysOnErrorSampleRate: 1.0, // 100% on errors

      // Release tracking
      release: import.meta.env.VITE_APP_VERSION,

      // Filter out known issues
      beforeSend(event, hint) {
        // Don't send events from development
        if (import.meta.env.DEV) {
          return null;
        }

        // Filter out specific errors if needed
        if (event.exception) {
          const error = hint.originalException as Error;
          if (error?.message?.includes('ResizeObserver')) {
            return null; // Ignore benign ResizeObserver errors
          }
        }

        return event;
      },
    });
  }
}

// Error boundary fallback
export function SentryErrorBoundaryFallback({ error, resetError }: any) {
  return (
    <div className="min-h-screen flex items-center justify-center p-4">
      <div className="max-w-md w-full bg-white rounded-lg shadow-lg p-6">
        <h2 className="text-2xl font-bold text-red-600 mb-4">
          Something went wrong
        </h2>
        <p className="text-gray-700 mb-4">
          We've been notified and will fix this as soon as possible.
        </p>
        <button
          onClick={resetError}
          className="w-full bg-blue-500 text-white py-2 px-4 rounded hover:bg-blue-600"
        >
          Try again
        </button>
      </div>
    </div>
  );
}
```

#### 5. Update Main Entry Point

Edit `src/main.tsx`:

```typescript
import React from 'react'
import ReactDOM from 'react-dom/client'
import * as Sentry from "@sentry/react";
import { initSentry, SentryErrorBoundaryFallback } from './lib/sentry'
import App from './App.tsx'
import './index.css'

// Initialize Sentry
initSentry();

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <Sentry.ErrorBoundary fallback={SentryErrorBoundaryFallback}>
      <App />
    </Sentry.ErrorBoundary>
  </React.StrictMode>,
)
```

#### 6. Configure Environment Variables

Update `.env.example`:

```bash
# Sentry Configuration
SENTRY_DSN=https://your-sentry-dsn@sentry.io/project-id
SENTRY_ORG=your-org-name
SENTRY_PROJECT=foodiepass-frontend
SENTRY_AUTH_TOKEN=your-auth-token

# App Version (for release tracking)
VITE_APP_VERSION=1.0.0
```

Add to `.env.local` (development):

```bash
SENTRY_DSN=your-actual-dsn
# Don't set other vars in development to avoid uploading source maps
```

Add to Vercel environment variables (production):

```
SENTRY_DSN=your-actual-dsn
SENTRY_ORG=your-org-name
SENTRY_PROJECT=foodiepass-frontend
SENTRY_AUTH_TOKEN=your-auth-token
VITE_APP_VERSION=1.0.0
```

### What Sentry Will Capture

#### Automatic Error Tracking

- **JavaScript Errors**: Uncaught exceptions
- **Promise Rejections**: Unhandled promise errors
- **API Errors**: Failed HTTP requests (if instrumented)
- **Component Errors**: React component crashes

#### Performance Monitoring

- **Page Loads**: Initial page load time
- **Transactions**: API calls and navigation
- **Web Vitals**: LCP, FID, CLS metrics

#### Session Replay (Optional)

- **User Sessions**: Visual replay of user interactions before error
- **Privacy**: Automatically masks sensitive data

### Custom Error Tracking

#### Track Specific Errors

```typescript
import * as Sentry from "@sentry/react";

try {
  // Risky operation
} catch (error) {
  Sentry.captureException(error);
}
```

#### Add Context

```typescript
Sentry.setUser({
  id: scanId,
  segment: abGroup, // CONTROL or TREATMENT
});

Sentry.setTag("feature", "menu-scan");
Sentry.setContext("menu", {
  itemCount: items.length,
  language: targetLanguage,
  currency: targetCurrency,
});
```

#### Track Hypothesis Validation

```typescript
// Track A/B group assignment
Sentry.addBreadcrumb({
  category: "ab-test",
  message: `User assigned to ${abGroup} group`,
  level: "info",
});

// Track survey submission
Sentry.addBreadcrumb({
  category: "survey",
  message: `Survey submitted: ${hasConfidence}`,
  level: "info",
});
```

## Vercel Analytics (Built-in)

### What It Tracks

- **Page Views**: Number of visits
- **Unique Visitors**: De-duplicated users
- **Top Pages**: Most visited routes
- **Referrers**: Where traffic comes from
- **Devices**: Desktop vs Mobile
- **Locations**: Geographic distribution

### Enable Vercel Analytics

1. Go to your project in Vercel
2. Navigate to "Analytics" tab
3. Click "Enable Analytics"
4. Free tier includes basic analytics

### Web Vitals Tracking

Vercel automatically tracks:

- **LCP** (Largest Contentful Paint): < 2.5s
- **FID** (First Input Delay): < 100ms
- **CLS** (Cumulative Layout Shift): < 0.1

## Custom Analytics (Optional)

### Track A/B Test Participation

```typescript
// src/lib/analytics.ts
export function trackABGroup(scanId: string, abGroup: 'CONTROL' | 'TREATMENT') {
  // Send to analytics service
  if (window.gtag) {
    window.gtag('event', 'ab_test_assignment', {
      scan_id: scanId,
      ab_group: abGroup,
    });
  }

  // Also track in Sentry
  Sentry.setTag('ab_group', abGroup);
}

export function trackSurveyResponse(scanId: string, hasConfidence: boolean) {
  if (window.gtag) {
    window.gtag('event', 'survey_response', {
      scan_id: scanId,
      has_confidence: hasConfidence,
    });
  }
}
```

### Usage in Components

```typescript
// In menu display component
useEffect(() => {
  if (scanData) {
    trackABGroup(scanData.scanId, scanData.abGroup);
  }
}, [scanData]);

// In survey modal
const handleSubmit = async (hasConfidence: boolean) => {
  await submitSurvey({ scanId, hasConfidence });
  trackSurveyResponse(scanId, hasConfidence);
};
```

## Dashboard Setup

### Sentry Dashboard

Create custom dashboards:

1. **Error Overview**:
   - Total errors
   - Error rate trend
   - Top errors by count

2. **Performance**:
   - Average response time
   - Slow transactions
   - Web Vitals

3. **Hypothesis Validation**:
   - A/B group distribution
   - Survey response rates by group
   - Error rates by group

### Alerts

Configure alerts for:

- **High Error Rate**: > 10 errors/minute
- **Slow API**: Average response time > 5 seconds
- **Failed Deployments**: Build or deploy failures

## Privacy Considerations

### Data Collection Policy

For MVP, collect:
- ✅ Error messages and stack traces
- ✅ Performance metrics
- ✅ A/B group assignment
- ✅ Survey responses (anonymous)

Do NOT collect:
- ❌ Uploaded menu images (keep on server only)
- ❌ Personal identifiable information
- ❌ Payment information (not applicable for MVP)

### GDPR Compliance

Sentry provides:
- Data encryption in transit and at rest
- Data retention policies (configurable)
- Data deletion APIs (if user requests)

### User Privacy

```typescript
// Mask sensitive data in Sentry
Sentry.init({
  beforeSend(event) {
    // Remove any sensitive data
    if (event.request?.data) {
      delete event.request.data.image; // Don't send image data
    }
    return event;
  },
});
```

## Monitoring Checklist

- [ ] Sentry account created
- [ ] Sentry SDK installed and initialized
- [ ] Environment variables configured
- [ ] Error boundary implemented
- [ ] Custom error tracking added
- [ ] Performance monitoring enabled
- [ ] Vercel Analytics enabled
- [ ] A/B test tracking implemented
- [ ] Survey tracking implemented
- [ ] Alerts configured
- [ ] Dashboard created
- [ ] Privacy policy updated

## Testing Monitoring Setup

### Test Sentry Integration

```typescript
// Add temporary test button in development
<button onClick={() => {
  throw new Error("Sentry test error");
}}>
  Test Sentry
</button>
```

1. Click button in development
2. Check Sentry dashboard
3. Verify error appears

### Test Performance Monitoring

1. Deploy to production
2. Use app normally
3. Check Sentry Performance tab
4. Verify transactions are tracked

## Cost Estimation

### Sentry Free Tier

- **Events**: 5,000 errors/month
- **Performance**: 10,000 transactions/month
- **Session Replay**: 50 sessions/month

Sufficient for MVP with <1000 users.

### Paid Tier (if needed)

- **Team**: $26/month
- **Events**: 50,000 errors/month
- **Performance**: 100,000 transactions/month

## Resources

- [Sentry React Documentation](https://docs.sentry.io/platforms/javascript/guides/react/)
- [Vercel Analytics](https://vercel.com/docs/analytics)
- [Web Vitals](https://web.dev/vitals/)
