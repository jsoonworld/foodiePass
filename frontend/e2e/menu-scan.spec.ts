import { test, expect } from '@playwright/test';
import path from 'path';

test.describe('FoodiePass Menu Scanner E2E', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
  });

  test('should load homepage with uploader UI', async ({ page }) => {
    // Check page title
    await expect(page).toHaveTitle(/FoodiePass/);

    // Check main heading
    const heading = page.getByRole('heading', { level: 1 });
    await expect(heading).toBeVisible();

    // Check uploader container
    const uploader = page.locator('[data-testid="menu-uploader"]');
    await expect(uploader).toBeVisible();

    // Check language selector
    const languageSelect = page.locator('select[name="targetLanguage"]');
    await expect(languageSelect).toBeVisible();

    // Check currency selector
    const currencySelect = page.locator('select[name="targetCurrency"]');
    await expect(currencySelect).toBeVisible();

    // Check scan button
    const scanButton = page.getByRole('button', { name: /scan menu/i });
    await expect(scanButton).toBeVisible();
    await expect(scanButton).toBeDisabled(); // Should be disabled without image
  });

  test('should upload image and enable scan button', async ({ page }) => {
    // Create sample menu fixture if not exists
    const sampleImagePath = path.join(__dirname, 'fixtures', 'sample-menu.jpg');

    // Locate file input
    const fileInput = page.locator('input[type="file"]');

    // Upload file
    await fileInput.setInputFiles(sampleImagePath);

    // Wait for preview to appear
    const preview = page.locator('[data-testid="image-preview"]');
    await expect(preview).toBeVisible({ timeout: 5000 });

    // Check scan button is now enabled
    const scanButton = page.getByRole('button', { name: /scan menu/i });
    await expect(scanButton).toBeEnabled();
  });

  test('should perform menu scan and show results', async ({ page }) => {
    const sampleImagePath = path.join(__dirname, 'fixtures', 'sample-menu.jpg');

    // Upload image
    const fileInput = page.locator('input[type="file"]');
    await fileInput.setInputFiles(sampleImagePath);

    // Wait for preview
    await page.locator('[data-testid="image-preview"]').waitFor({ state: 'visible' });

    // Select language and currency
    await page.selectOption('select[name="targetLanguage"]', 'ko');
    await page.selectOption('select[name="targetCurrency"]', 'KRW');

    // Click scan button
    const scanButton = page.getByRole('button', { name: /scan menu/i });
    await scanButton.click();

    // Wait for loading state
    const loadingIndicator = page.locator('[data-testid="loading"]');
    await expect(loadingIndicator).toBeVisible();

    // Wait for results (timeout 10s for API call)
    const results = page.locator('[data-testid="scan-results"]');
    await expect(results).toBeVisible({ timeout: 10000 });

    // Check that menu items are displayed
    const menuItems = page.locator('[data-testid="menu-item"]');
    await expect(menuItems.first()).toBeVisible();
  });

  test('should show Control UI (text-only) for Control group', async ({ page }) => {
    // Mock API response to force Control group
    await page.route('**/api/menus/scan', async (route) => {
      const response = await route.fetch();
      const json = await response.json();
      json.abGroup = 'CONTROL';
      await route.fulfill({ response, json });
    });

    const sampleImagePath = path.join(__dirname, 'fixtures', 'sample-menu.jpg');

    // Upload and scan
    const fileInput = page.locator('input[type="file"]');
    await fileInput.setInputFiles(sampleImagePath);
    await page.locator('[data-testid="image-preview"]').waitFor({ state: 'visible' });

    const scanButton = page.getByRole('button', { name: /scan menu/i });
    await scanButton.click();

    // Wait for results
    const results = page.locator('[data-testid="scan-results"]');
    await expect(results).toBeVisible({ timeout: 10000 });

    // Check Control UI: no food images
    const foodImages = page.locator('[data-testid="food-image"]');
    await expect(foodImages).toHaveCount(0);

    // Check text content is visible
    const menuItem = page.locator('[data-testid="menu-item"]').first();
    await expect(menuItem).toContainText(/원/); // Korean currency
  });

  test('should show Treatment UI (visual menu) for Treatment group', async ({ page }) => {
    // Mock API response to force Treatment group
    await page.route('**/api/menus/scan', async (route) => {
      const response = await route.fetch();
      const json = await response.json();
      json.abGroup = 'TREATMENT';
      // Ensure menu items have food info
      if (json.menuItems && json.menuItems.length > 0) {
        json.menuItems = json.menuItems.map((item: any) => ({
          ...item,
          foodInfo: {
            name: 'Sample Dish',
            imageUrl: 'https://example.com/food.jpg',
            description: 'Delicious sample dish',
            relevanceScore: 0.85
          }
        }));
      }
      await route.fulfill({ response, json });
    });

    const sampleImagePath = path.join(__dirname, 'fixtures', 'sample-menu.jpg');

    // Upload and scan
    const fileInput = page.locator('input[type="file"]');
    await fileInput.setInputFiles(sampleImagePath);
    await page.locator('[data-testid="image-preview"]').waitFor({ state: 'visible' });

    const scanButton = page.getByRole('button', { name: /scan menu/i });
    await scanButton.click();

    // Wait for results
    const results = page.locator('[data-testid="scan-results"]');
    await expect(results).toBeVisible({ timeout: 10000 });

    // Check Treatment UI: food images should be visible
    const foodImages = page.locator('[data-testid="food-image"]');
    await expect(foodImages.first()).toBeVisible({ timeout: 5000 });

    // Check food descriptions
    const foodDescription = page.locator('[data-testid="food-description"]');
    await expect(foodDescription.first()).toBeVisible();
  });

  test('should show survey modal after 5 seconds', async ({ page }) => {
    const sampleImagePath = path.join(__dirname, 'fixtures', 'sample-menu.jpg');

    // Upload and scan
    const fileInput = page.locator('input[type="file"]');
    await fileInput.setInputFiles(sampleImagePath);
    await page.locator('[data-testid="image-preview"]').waitFor({ state: 'visible' });

    const scanButton = page.getByRole('button', { name: /scan menu/i });
    await scanButton.click();

    // Wait for results
    const results = page.locator('[data-testid="scan-results"]');
    await expect(results).toBeVisible({ timeout: 10000 });

    // Survey modal should not be visible immediately
    const surveyModal = page.locator('[data-testid="survey-modal"]');
    await expect(surveyModal).not.toBeVisible();

    // Wait 5 seconds and check survey modal appears
    await page.waitForTimeout(5500);
    await expect(surveyModal).toBeVisible();

    // Check survey question
    const surveyQuestion = surveyModal.locator('h2');
    await expect(surveyQuestion).toBeVisible();

    // Check Yes/No buttons
    const yesButton = surveyModal.getByRole('button', { name: /yes|예/i });
    const noButton = surveyModal.getByRole('button', { name: /no|아니/i });
    await expect(yesButton).toBeVisible();
    await expect(noButton).toBeVisible();
  });

  test('should submit survey response', async ({ page }) => {
    const sampleImagePath = path.join(__dirname, 'fixtures', 'sample-menu.jpg');

    // Upload and scan
    const fileInput = page.locator('input[type="file"]');
    await fileInput.setInputFiles(sampleImagePath);
    await page.locator('[data-testid="image-preview"]').waitFor({ state: 'visible' });

    const scanButton = page.getByRole('button', { name: /scan menu/i });
    await scanButton.click();

    // Wait for results
    const results = page.locator('[data-testid="scan-results"]');
    await expect(results).toBeVisible({ timeout: 10000 });

    // Wait for survey modal
    await page.waitForTimeout(5500);
    const surveyModal = page.locator('[data-testid="survey-modal"]');
    await expect(surveyModal).toBeVisible();

    // Click Yes button
    const yesButton = surveyModal.getByRole('button', { name: /yes|예/i });
    await yesButton.click();

    // Check thank you message appears
    const thankYouMessage = surveyModal.getByText(/thank you|감사합니다/i);
    await expect(thankYouMessage).toBeVisible({ timeout: 3000 });

    // Survey modal should close after a few seconds
    await page.waitForTimeout(3000);
    await expect(surveyModal).not.toBeVisible();
  });

  test('should be responsive on mobile viewport', async ({ page }) => {
    // Set mobile viewport
    await page.setViewportSize({ width: 375, height: 667 }); // iPhone SE

    // Check uploader is visible and properly sized
    const uploader = page.locator('[data-testid="menu-uploader"]');
    await expect(uploader).toBeVisible();

    // Upload image
    const sampleImagePath = path.join(__dirname, 'fixtures', 'sample-menu.jpg');
    const fileInput = page.locator('input[type="file"]');
    await fileInput.setInputFiles(sampleImagePath);

    // Check preview fits mobile screen
    const preview = page.locator('[data-testid="image-preview"]');
    await expect(preview).toBeVisible();

    // Check scan button is accessible
    const scanButton = page.getByRole('button', { name: /scan menu/i });
    await expect(scanButton).toBeVisible();
    await expect(scanButton).toBeEnabled();
  });
});
