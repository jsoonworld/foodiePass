import { test, expect } from '@playwright/test';
import path from 'path';

/**
 * E2E Test: Control Group User Flow
 *
 * Tests the complete user journey for CONTROL group users who see text-only menu.
 * Validates H1 and H3 hypotheses by ensuring Control group gets text + price only.
 */

test.describe('Control Group User Flow', () => {
  test('should complete full flow with text-only menu (Control group)', async ({ page }) => {
    // 1. Navigate to home page
    await page.goto('/');

    // Verify upload page is displayed
    await expect(page.getByText(/upload.*menu/i)).toBeVisible();

    // 2. Upload menu image
    const testImagePath = path.join(__dirname, 'fixtures', 'sample-menu.jpg');
    const fileInput = page.locator('input[type="file"]');
    await fileInput.setInputFiles(testImagePath);

    // 3. Select language and currency
    await page.selectOption('select[name="targetLanguage"]', 'ko');
    await page.selectOption('select[name="targetCurrency"]', 'KRW');

    // 4. Click scan button
    await page.click('button:has-text("Scan Menu")');

    // 5. Wait for processing (should be < 5 seconds per requirements)
    await expect(page.getByText(/processing/i)).toBeVisible();

    // Wait for results page
    await page.waitForURL(/\/menu\/\w+/, { timeout: 10000 });

    // 6. Verify Control UI (text-only)
    // Should see translated menu items
    const menuItems = page.locator('[data-testid="menu-item"]');
    await expect(menuItems.first()).toBeVisible();

    // Should see text and price
    await expect(page.getByText(/₩/)).toBeVisible(); // Currency symbol

    // Should NOT see food images (key difference for Control group)
    const foodImages = page.locator('[data-testid="food-image"]');
    await expect(foodImages).toHaveCount(0);

    // Should NOT see food descriptions
    const foodDescriptions = page.locator('[data-testid="food-description"]');
    await expect(foodDescriptions).toHaveCount(0);

    // 7. Wait for survey modal (should appear after 5 seconds)
    await page.waitForTimeout(5000);

    const surveyModal = page.locator('[data-testid="survey-modal"]');
    await expect(surveyModal).toBeVisible();

    // Verify survey question
    await expect(page.getByText(/확신을 갖고 주문할 수 있습니까/i)).toBeVisible();

    // 8. Submit survey (No for Control group - testing H3)
    await page.click('button:has-text("No")');

    // Verify thank you message
    await expect(page.getByText(/감사합니다/i)).toBeVisible();

    // Modal should close
    await expect(surveyModal).not.toBeVisible({ timeout: 3000 });
  });

  test('should display menu items with correct format (Control)', async ({ page }) => {
    // Setup: Upload and get to results page
    await page.goto('/');
    const testImagePath = path.join(__dirname, 'fixtures', 'sample-menu.jpg');
    const fileInput = page.locator('input[type="file"]');
    await fileInput.setInputFiles(testImagePath);
    await page.selectOption('select[name="targetLanguage"]', 'ko');
    await page.selectOption('select[name="targetCurrency"]', 'KRW');
    await page.click('button:has-text("Scan Menu")');
    await page.waitForURL(/\/menu\/\w+/);

    // Verify menu item structure
    const firstMenuItem = page.locator('[data-testid="menu-item"]').first();

    // Should have original name
    await expect(firstMenuItem.locator('[data-testid="original-name"]')).toBeVisible();

    // Should have translated name
    await expect(firstMenuItem.locator('[data-testid="translated-name"]')).toBeVisible();

    // Should have original price
    await expect(firstMenuItem.locator('[data-testid="original-price"]')).toBeVisible();

    // Should have converted price
    await expect(firstMenuItem.locator('[data-testid="converted-price"]')).toBeVisible();

    // Verify simple list format (no cards, no images)
    await expect(firstMenuItem).toHaveCSS('display', /flex|block/);
  });

  test('should measure confidence score for Control group', async ({ page, context }) => {
    // This test ensures we can track Control group confidence responses
    let surveySubmitted = false;

    // Intercept survey API call
    await page.route('**/api/surveys', (route) => {
      const postData = route.request().postData();
      if (postData) {
        const data = JSON.parse(postData);
        expect(data).toHaveProperty('scanId');
        expect(data).toHaveProperty('hasConfidence');
        expect(typeof data.hasConfidence).toBe('boolean');
        surveySubmitted = true;
      }
      route.continue();
    });

    // Complete flow
    await page.goto('/');
    const testImagePath = path.join(__dirname, 'fixtures', 'sample-menu.jpg');
    const fileInput = page.locator('input[type="file"]');
    await fileInput.setInputFiles(testImagePath);
    await page.selectOption('select[name="targetLanguage"]', 'ko');
    await page.selectOption('select[name="targetCurrency"]', 'KRW');
    await page.click('button:has-text("Scan Menu")');
    await page.waitForURL(/\/menu\/\w+/);

    // Wait for survey and submit
    await page.waitForTimeout(5000);
    await page.click('button:has-text("No")');

    // Verify API was called
    await page.waitForTimeout(1000); // Wait for API call
    expect(surveySubmitted).toBe(true);
  });
});
