# E2E Testing Guide

## Overview

FoodiePass uses Playwright for end-to-end testing to validate the complete user journey and ensure hypothesis validation integrity.

## Test Coverage

### 1. Control Group Flow (`control-flow.spec.ts`)
Tests the user journey for Control group users who see text-only menu.

**Key Validations**:
- Upload and scan functionality
- Text + price display only
- NO food images or descriptions
- Survey modal appears after 5 seconds
- Survey submission with expected "No" response

**Hypothesis Validation**: H1, H3 (Control group baseline)

### 2. Treatment Group Flow (`treatment-flow.spec.ts`)
Tests the user journey for Treatment group users who see visual menu.

**Key Validations**:
- Upload and scan functionality
- Visual menu with food images
- Food descriptions visible
- Text + price display
- Card-style layout with shadows
- Survey modal appears after 5 seconds
- Survey submission with expected "Yes" response

**Hypothesis Validation**: H1, H3 (Treatment group comparison)

### 3. Error Handling (`error-handling.spec.ts`)
Tests various error scenarios and user-friendly error messages.

**Scenarios**:
- Invalid file type upload
- No file selected
- API error response (500)
- Network timeout
- Network failure (offline mode)
- Empty menu response
- Retry after error
- File size limit validation
- Malformed API response
- User-friendly error messages

**Hypothesis Validation**: Technical reliability (H2)

### 4. Survey Submission (`survey-submission.spec.ts`)
Tests survey modal behavior and submission flow.

**Key Validations**:
- Survey appears exactly 5 seconds after results load
- Survey question displays correctly
- Yes/No buttons functional
- API submission with correct data
- Thank you message display
- Modal closes after submission
- Survey API error handling
- Duplicate submission prevention
- Correct scanId inclusion
- Button disable during submission

**Hypothesis Validation**: H3 (confidence measurement integrity)

## Setup

### Prerequisites

```bash
cd frontend
npm install
```

### Install Playwright Browsers

```bash
npx playwright install chromium
```

### Add Test Fixtures

Before running tests, add a sample menu image:

```bash
# Add your own menu image
cp /path/to/your/menu.jpg e2e/fixtures/sample-menu.jpg
```

**Requirements**:
- Format: JPEG
- Size: < 5MB
- Content: Restaurant menu with readable text
- Language: Korean or English preferred

## Running Tests

### Run All E2E Tests

```bash
npm run test:e2e
```

### Run in UI Mode (Recommended for Development)

```bash
npm run test:e2e:ui
```

Interactive UI allows you to:
- See test execution in real-time
- Debug failures visually
- Rerun specific tests
- View traces and screenshots

### Run with Browser Visible

```bash
npm run test:e2e:headed
```

Useful for debugging specific interactions.

### View Test Report

```bash
npm run test:e2e:report
```

Opens HTML report with:
- Test results summary
- Failure screenshots
- Execution traces
- Performance metrics

## Test Structure

```
frontend/
├── playwright.config.ts      # Playwright configuration
├── e2e/
│   ├── control-flow.spec.ts
│   ├── treatment-flow.spec.ts
│   ├── error-handling.spec.ts
│   ├── survey-submission.spec.ts
│   └── fixtures/
│       ├── README.md
│       ├── sample-menu.jpg   # User-provided
│       └── invalid-file.txt
```

## Test Data Attributes

Components use `data-testid` attributes for stable test selectors:

```tsx
// Menu items
data-testid="menu-item"
data-testid="menu-container"

// Control UI
data-testid="original-name"
data-testid="translated-name"
data-testid="original-price"
data-testid="converted-price"

// Treatment UI (additional)
data-testid="food-image"
data-testid="food-description"

// Survey
data-testid="survey-modal"
```

## CI/CD Integration

### GitHub Actions Example

```yaml
name: E2E Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3
      - name: Install dependencies
        run: npm ci
      - name: Install Playwright
        run: npx playwright install --with-deps chromium
      - name: Add test fixtures
        run: |
          # Download or generate test menu image
          # cp test-assets/menu.jpg e2e/fixtures/sample-menu.jpg
      - name: Run E2E tests
        run: npm run test:e2e
      - name: Upload test report
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: playwright-report
          path: playwright-report/
```

## Best Practices

### 1. Use Test IDs
Always use `data-testid` instead of CSS classes or text content for selectors.

```typescript
// ✅ Good
await page.locator('[data-testid="menu-item"]').click();

// ❌ Bad
await page.locator('.menu-card').click();
```

### 2. Wait for Navigation
Always wait for URL changes after navigation.

```typescript
await page.click('button:has-text("Scan Menu")');
await page.waitForURL(/\/menu\/\w+/);
```

### 3. Mock External APIs (Optional)
For isolated testing, mock backend APIs.

```typescript
await page.route('**/api/menus/scan', (route) => {
  route.fulfill({
    status: 200,
    body: JSON.stringify({ /* mock data */ }),
  });
});
```

### 4. Handle Asynchronous Behavior
Use appropriate timeouts for async operations.

```typescript
// Survey appears after 5 seconds
await page.waitForTimeout(5000);
await expect(surveyModal).toBeVisible();
```

### 5. Screenshot on Failure
Playwright automatically captures screenshots on failure (configured in `playwright.config.ts`).

## Debugging Failed Tests

### 1. Run in UI Mode
```bash
npm run test:e2e:ui
```

### 2. Run with Headed Browser
```bash
npm run test:e2e:headed
```

### 3. Use Playwright Inspector
```bash
PWDEBUG=1 npm run test:e2e
```

### 4. View Trace
```bash
npm run test:e2e:report
# Click on failed test → View trace
```

## Performance Targets

Based on H2 (technical hypothesis):

- **Total scan time**: < 5 seconds
- **Survey display**: Exactly 5 seconds after results load
- **API response**: < 3 seconds (normal conditions)

Tests validate these performance requirements.

## A/B Testing Integrity

Critical: Tests ensure Control and Treatment groups see exactly the right UI.

**Control Group Must NOT See**:
- Food images (`data-testid="food-image"`)
- Food descriptions (`data-testid="food-description"`)
- Card-style layout

**Treatment Group Must See**:
- Food images
- Food descriptions
- Card-style layout with shadows
- All Control group elements (text, prices)

## Hypothesis Validation Coverage

| Hypothesis | Test Coverage |
|------------|---------------|
| **H1**: Visual menu increases confidence | Control vs Treatment UI validation |
| **H2**: Technical feasibility | Error handling, performance tests |
| **H3**: 2x confidence increase | Survey submission tracking |

## Troubleshooting

### Issue: Test fixtures not found
**Solution**: Add `sample-menu.jpg` to `e2e/fixtures/`

### Issue: Timeout errors
**Solution**: Check backend is running, increase timeout in `playwright.config.ts`

### Issue: Flaky tests
**Solution**: Use proper wait conditions instead of fixed timeouts

### Issue: Can't see browser
**Solution**: Run with `npm run test:e2e:headed`

## Future Enhancements

- [ ] Visual regression testing
- [ ] Mobile device testing
- [ ] Cross-browser testing (Firefox, Safari)
- [ ] Performance profiling
- [ ] Accessibility testing (WCAG)
- [ ] Load testing with multiple concurrent users

## Resources

- [Playwright Documentation](https://playwright.dev/)
- [Best Practices](https://playwright.dev/docs/best-practices)
- [Debugging Guide](https://playwright.dev/docs/debug)
