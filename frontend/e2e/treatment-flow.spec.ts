import { test, expect } from '@playwright/test';
import path from 'path';

/**
 * E2E Test: Treatment Group User Flow
 *
 * Tests the complete user journey for TREATMENT group users who see visual menu
 * with photos, descriptions, text, and price.
 * Validates H1 and H3 hypotheses by ensuring Treatment group gets rich visual content.
 */

test.describe('Treatment Group User Flow', () => {
  test('should complete full flow with visual menu (Treatment group)', async ({ page }) => {
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

    // 5. Wait for processing
    await expect(page.getByText(/processing/i)).toBeVisible();

    // Wait for results page
    await page.waitForURL(/\/menu\/\w+/, { timeout: 10000 });

    // 6. Verify Treatment UI (visual menu with photos)
    const menuItems = page.locator('[data-testid="menu-item"]');
    await expect(menuItems.first()).toBeVisible();

    // Should see text and price (same as Control)
    await expect(page.getByText(/₩/)).toBeVisible();

    // Should see food images (key difference for Treatment group)
    const foodImages = page.locator('[data-testid="food-image"]');
    await expect(foodImages.first()).toBeVisible();
    await expect(foodImages).toHaveCount(await menuItems.count());

    // Should see food descriptions
    const foodDescriptions = page.locator('[data-testid="food-description"]');
    await expect(foodDescriptions.first()).toBeVisible();

    // Verify card-style layout (vs simple list in Control)
    const firstMenuItem = menuItems.first();
    await expect(firstMenuItem).toHaveClass(/card|shadow/);

    // 7. Wait for survey modal (should appear after 5 seconds)
    await page.waitForTimeout(5000);

    const surveyModal = page.locator('[data-testid="survey-modal"]');
    await expect(surveyModal).toBeVisible();

    // Verify survey question
    await expect(page.getByText(/확신을 갖고 주문할 수 있습니까/i)).toBeVisible();

    // 8. Submit survey (Yes for Treatment group - testing H3)
    await page.click('button:has-text("Yes")');

    // Verify thank you message
    await expect(page.getByText(/감사합니다/i)).toBeVisible();

    // Modal should close
    await expect(surveyModal).not.toBeVisible({ timeout: 3000 });
  });

  test('should display visual menu items with all components (Treatment)', async ({ page }) => {
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

    // Should have food image
    const foodImage = firstMenuItem.locator('[data-testid="food-image"]');
    await expect(foodImage).toBeVisible();
    await expect(foodImage).toHaveAttribute('src', /.+/); // Has valid src

    // Should have food description
    const foodDescription = firstMenuItem.locator('[data-testid="food-description"]');
    await expect(foodDescription).toBeVisible();
    const descriptionText = await foodDescription.textContent();
    expect(descriptionText?.length).toBeGreaterThan(10); // Non-empty description

    // Should have original name
    await expect(firstMenuItem.locator('[data-testid="original-name"]')).toBeVisible();

    // Should have translated name
    await expect(firstMenuItem.locator('[data-testid="translated-name"]')).toBeVisible();

    // Should have original price
    await expect(firstMenuItem.locator('[data-testid="original-price"]')).toBeVisible();

    // Should have converted price
    await expect(firstMenuItem.locator('[data-testid="converted-price"]')).toBeVisible();

    // Verify card-style layout with shadow effect
    await expect(firstMenuItem).toHaveClass(/shadow|card/);
  });

  test('should measure confidence score for Treatment group', async ({ page }) => {
    // This test ensures we can track Treatment group confidence responses
    let surveySubmitted = false;
    let hasConfidence: boolean | null = null;

    // Intercept survey API call
    await page.route('**/api/surveys', (route) => {
      const postData = route.request().postData();
      if (postData) {
        const data = JSON.parse(postData);
        expect(data).toHaveProperty('scanId');
        expect(data).toHaveProperty('hasConfidence');
        expect(typeof data.hasConfidence).toBe('boolean');
        hasConfidence = data.hasConfidence;
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

    // Wait for survey and submit Yes (Treatment group should be more confident)
    await page.waitForTimeout(5000);
    await page.click('button:has-text("Yes")');

    // Verify API was called with correct data
    await page.waitForTimeout(1000);
    expect(surveySubmitted).toBe(true);
    expect(hasConfidence).toBe(true);
  });

  test('should handle missing food images gracefully', async ({ page }) => {
    // Test that Treatment group still works even if some images are missing
    await page.goto('/');
    const testImagePath = path.join(__dirname, 'fixtures', 'sample-menu.jpg');
    const fileInput = page.locator('input[type="file"]');
    await fileInput.setInputFiles(testImagePath);
    await page.selectOption('select[name="targetLanguage"]', 'ko');
    await page.selectOption('select[name="targetCurrency"]', 'KRW');
    await page.click('button:has-text("Scan Menu")');
    await page.waitForURL(/\/menu\/\w+/);

    // Check for placeholder images when food images are not available
    const menuItems = page.locator('[data-testid="menu-item"]');
    const firstImage = menuItems.first().locator('[data-testid="food-image"]');

    // Should have either real image or placeholder
    await expect(firstImage).toBeVisible();

    // Check if image loads or shows placeholder
    const src = await firstImage.getAttribute('src');
    expect(src).toBeTruthy();
    expect(src).toMatch(/http|\/placeholder|data:image/);
  });

  test('should display all menu items in grid/card layout', async ({ page }) => {
    await page.goto('/');
    const testImagePath = path.join(__dirname, 'fixtures', 'sample-menu.jpg');
    const fileInput = page.locator('input[type="file"]');
    await fileInput.setInputFiles(testImagePath);
    await page.selectOption('select[name="targetLanguage"]', 'ko');
    await page.selectOption('select[name="targetCurrency"]', 'KRW');
    await page.click('button:has-text("Scan Menu")');
    await page.waitForURL(/\/menu\/\w+/);

    // Verify grid layout
    const menuContainer = page.locator('[data-testid="menu-container"]');
    await expect(menuContainer).toBeVisible();

    // Should use grid or flex layout
    const display = await menuContainer.evaluate((el) =>
      window.getComputedStyle(el).display
    );
    expect(display).toMatch(/grid|flex/);

    // All items should be visible
    const menuItems = page.locator('[data-testid="menu-item"]');
    const count = await menuItems.count();
    expect(count).toBeGreaterThan(0);

    // Each item should have proper spacing
    const firstItem = menuItems.first();
    const margin = await firstItem.evaluate((el) =>
      window.getComputedStyle(el).margin
    );
    expect(margin).not.toBe('0px');
  });
});
