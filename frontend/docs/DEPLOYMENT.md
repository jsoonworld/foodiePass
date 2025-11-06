# Deployment Guide

## Overview

FoodiePass frontend is configured for deployment on Vercel, but can also be deployed on Netlify or AWS S3 + CloudFront.

## Vercel Deployment (Recommended)

### Prerequisites

1. Vercel account (free tier is sufficient)
2. GitHub repository access
3. Backend API deployed and accessible

### Step 1: Import Project

1. Go to [Vercel Dashboard](https://vercel.com/dashboard)
2. Click "Add New" → "Project"
3. Import your GitHub repository
4. Select the `frontend` directory as the root

### Step 2: Configure Build Settings

Vercel should auto-detect Vite, but verify:

- **Framework Preset**: Vite
- **Build Command**: `npm run build`
- **Output Directory**: `dist`
- **Install Command**: `npm install`

### Step 3: Configure Environment Variables

Add the following environment variables in Vercel:

| Variable | Value | Description |
|----------|-------|-------------|
| `NEXT_PUBLIC_API_URL` | `https://api.foodiepass.com` | Backend API URL |
| `NEXT_PUBLIC_APP_URL` | Auto-detected | Frontend URL |

**Production Example**:
```
NEXT_PUBLIC_API_URL=https://api.foodiepass.com
```

**Development/Preview**:
```
NEXT_PUBLIC_API_URL=https://dev-api.foodiepass.com
```

### Step 4: Deploy

1. Click "Deploy"
2. Wait for build to complete (~2-3 minutes)
3. Vercel will provide a deployment URL

### Step 5: Custom Domain (Optional)

1. Go to Project Settings → Domains
2. Add your custom domain (e.g., `app.foodiepass.com`)
3. Configure DNS records as instructed

## Netlify Deployment (Alternative)

### Step 1: Create `netlify.toml`

```toml
[build]
  command = "npm run build"
  publish = "dist"

[[redirects]]
  from = "/*"
  to = "/index.html"
  status = 200

[context.production.environment]
  NEXT_PUBLIC_API_URL = "https://api.foodiepass.com"
```

### Step 2: Deploy

1. Go to [Netlify](https://www.netlify.com/)
2. Import from GitHub
3. Select `frontend` directory
4. Deploy

## AWS S3 + CloudFront (Advanced)

### Prerequisites

- AWS account
- AWS CLI configured
- S3 bucket created
- CloudFront distribution configured

### Build and Upload

```bash
# Build
npm run build

# Upload to S3
aws s3 sync dist/ s3://your-bucket-name/ --delete

# Invalidate CloudFront cache
aws cloudfront create-invalidation --distribution-id YOUR_DIST_ID --paths "/*"
```

## Environment Variables

### Local Development

Create `.env.local`:

```bash
cp .env.example .env.local
```

Edit `.env.local`:

```
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_APP_URL=http://localhost:3000
```

### Production

**Required**:
- `NEXT_PUBLIC_API_URL`: Backend API endpoint

**Optional**:
- `SENTRY_DSN`: Error monitoring
- `NEXT_PUBLIC_ENABLE_ANALYTICS`: Feature flag

## Build Optimization

### Production Build

```bash
npm run build
```

### Preview Production Build Locally

```bash
npm run preview
```

This serves the production build locally for testing.

## Performance Optimization

### Code Splitting

Vite automatically code-splits by route. Lazy loading is implemented for:
- Heavy components
- Third-party libraries

### Asset Optimization

- Images: Auto-optimized by Vite
- Fonts: Preloaded in index.html
- CSS: Minified and tree-shaken

### Caching Strategy

Vercel/Netlify automatically configure:
- Static assets: 1 year cache
- index.html: No cache
- API calls: No cache (controlled by backend)

## Monitoring and Analytics

### Vercel Analytics (Built-in)

Vercel provides built-in analytics:
- Page views
- Performance metrics (Web Vitals)
- Geographic distribution

Enable in Vercel dashboard (free tier included).

### Sentry Error Monitoring (Optional)

1. Create Sentry project
2. Add Sentry DSN to environment variables
3. Install Sentry SDK:

```bash
npm install @sentry/react
```

4. Initialize in `src/main.tsx`:

```typescript
import * as Sentry from "@sentry/react";

Sentry.init({
  dsn: import.meta.env.SENTRY_DSN,
  environment: import.meta.env.MODE,
  tracesSampleRate: 1.0,
});
```

## CI/CD Pipeline

### GitHub Actions Example

Create `.github/workflows/deploy.yml`:

```yaml
name: Deploy to Vercel

on:
  push:
    branches:
      - main
      - develop

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
          cache: 'npm'
          cache-dependency-path: frontend/package-lock.json

      - name: Install dependencies
        working-directory: ./frontend
        run: npm ci

      - name: Run lint
        working-directory: ./frontend
        run: npm run lint

      - name: Build
        working-directory: ./frontend
        run: npm run build
        env:
          NEXT_PUBLIC_API_URL: ${{ secrets.API_URL }}

      - name: Deploy to Vercel
        uses: amondnet/vercel-action@v20
        with:
          vercel-token: ${{ secrets.VERCEL_TOKEN }}
          vercel-org-id: ${{ secrets.VERCEL_ORG_ID }}
          vercel-project-id: ${{ secrets.VERCEL_PROJECT_ID }}
          working-directory: ./frontend
```

## Rollback Strategy

### Vercel Rollback

1. Go to Deployments tab
2. Find previous successful deployment
3. Click "..." → "Promote to Production"

### Manual Rollback

```bash
# Revert to previous commit
git revert HEAD
git push origin main

# Vercel auto-deploys
```

## Health Checks

### Post-Deployment Verification

1. **Load homepage**: `https://your-domain.com`
2. **Test upload flow**: Upload sample menu
3. **Verify API connectivity**: Check network tab
4. **Test A/B groups**: Upload multiple times, verify different UIs
5. **Test survey**: Wait 5 seconds, submit survey

### Automated Health Checks (Optional)

Use GitHub Actions to run E2E tests against production:

```yaml
name: Production Health Check

on:
  schedule:
    - cron: '0 */6 * * *'  # Every 6 hours

jobs:
  health-check:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Setup Node.js
        uses: actions/setup-node@v3
      - name: Install dependencies
        run: npm ci
      - name: Install Playwright
        run: npx playwright install chromium
      - name: Run E2E tests
        run: npm run test:e2e
        env:
          PLAYWRIGHT_BASE_URL: https://your-production-url.com
```

## Security Considerations

### CORS Configuration

Backend must allow frontend domain:

```java
// Backend Spring Boot
@CrossOrigin(origins = "https://app.foodiepass.com", allowCredentials = "true")
```

### Environment Variables

**Never commit**:
- `.env.local`
- `.env.production`
- Any file with real API keys or secrets

**Do commit**:
- `.env.example` (template only)

### HTTPS

Always use HTTPS in production:
- Vercel provides automatic HTTPS
- Custom domains get free SSL certificates

## Troubleshooting

### Issue: API calls failing in production

**Check**:
1. `NEXT_PUBLIC_API_URL` is correct
2. Backend CORS allows frontend domain
3. Backend is deployed and accessible

### Issue: 404 on refresh

**Solution**: SPA routing configured in `vercel.json` rewrites

### Issue: Environment variables not working

**Solution**:
- Restart dev server after changing `.env.local`
- Redeploy on Vercel after changing env vars
- Variables must start with `NEXT_PUBLIC_` to be exposed to browser

### Issue: Build fails

**Check**:
1. Run `npm run build` locally
2. Fix TypeScript errors
3. Check build logs in Vercel/Netlify

## Performance Benchmarks

### Target Metrics (Lighthouse)

- **Performance**: > 90
- **Accessibility**: > 95
- **Best Practices**: > 90
- **SEO**: > 90

### Monitor with Vercel Analytics

- **First Contentful Paint (FCP)**: < 1.8s
- **Largest Contentful Paint (LCP)**: < 2.5s
- **Cumulative Layout Shift (CLS)**: < 0.1
- **First Input Delay (FID)**: < 100ms

## Cost Estimation

### Vercel (Free Tier)
- **Bandwidth**: 100GB/month
- **Build Minutes**: 6000 minutes/month
- **Deployments**: Unlimited
- **Team Size**: 1 member

Sufficient for MVP testing with <1000 users.

### Netlify (Free Tier)
- **Bandwidth**: 100GB/month
- **Build Minutes**: 300 minutes/month
- **Deployments**: Unlimited

### AWS (Estimated)
- **S3 Storage**: $0.023/GB (~$1/month)
- **CloudFront**: $0.085/GB transfer (~$8.50 for 100GB)
- **Total**: ~$10/month for moderate traffic

## Post-MVP Considerations

- [ ] CDN optimization
- [ ] Image optimization service (e.g., Cloudinary)
- [ ] Advanced monitoring (Datadog, New Relic)
- [ ] A/B testing platform integration
- [ ] User session recording (Hotjar, FullStory)
- [ ] Load balancing
- [ ] Auto-scaling

## Resources

- [Vercel Documentation](https://vercel.com/docs)
- [Vite Deployment Guide](https://vitejs.dev/guide/static-deploy.html)
- [Web Vitals](https://web.dev/vitals/)
