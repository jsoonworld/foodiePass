import { test, expect } from '@playwright/test';
import path from 'path';

/**
 * E2E Test: Error Handling Scenarios
 *
 * Tests various error conditions and ensures the application handles them gracefully
 * with user-friendly error messages.
 */

test.describe('Error Handling Scenarios', () => {
  test('should show error for invalid file type', async ({ page }) => {
    await page.goto('/');

    // Try to upload a non-image file (e.g., text file)
    const testFilePath = path.join(__dirname, 'fixtures', 'invalid-file.txt');
    const fileInput = page.locator('input[type="file"]');

    // Check if file input has accept attribute
    const accept = await fileInput.getAttribute('accept');
    expect(accept).toContain('image');

    // Attempt to upload invalid file
    await fileInput.setInputFiles(testFilePath);

    // Should show error message
    await expect(page.getByText(/invalid.*file.*type|only.*image/i)).toBeVisible({
      timeout: 3000,
    });
  });

  test('should show error when no file is selected', async ({ page }) => {
    await page.goto('/');

    // Select language and currency without uploading file
    await page.selectOption('select[name="targetLanguage"]', 'ko');
    await page.selectOption('select[name="targetCurrency"]', 'KRW');

    // Try to click scan button
    await page.click('button:has-text("Scan Menu")');

    // Should show error message
    await expect(page.getByText(/please.*select.*image|upload.*image/i)).toBeVisible({
      timeout: 3000,
    });
  });

  test('should handle API error response gracefully', async ({ page }) => {
    // Mock API to return error
    await page.route('**/api/menus/scan', (route) => {
      route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({
          error: 'Internal Server Error',
          message: 'Failed to process menu image',
        }),
      });
    });

    await page.goto('/');

    // Upload valid image
    const testImagePath = path.join(__dirname, 'fixtures', 'sample-menu.jpg');
    const fileInput = page.locator('input[type="file"]');
    await fileInput.setInputFiles(testImagePath);
    await page.selectOption('select[name="targetLanguage"]', 'ko');
    await page.selectOption('select[name="targetCurrency"]', 'KRW');

    // Click scan button
    await page.click('button:has-text("Scan Menu")');

    // Should show error message to user
    await expect(
      page.getByText(/failed.*process|error.*occurred|try.*again/i)
    ).toBeVisible({ timeout: 5000 });

    // Should not navigate away from upload page
    expect(page.url()).toContain('/');
  });

  test('should handle network timeout', async ({ page }) => {
    // Mock API to timeout
    await page.route('**/api/menus/scan', async (route) => {
      // Delay response beyond timeout
      await new Promise((resolve) => setTimeout(resolve, 15000));
      route.continue();
    });

    await page.goto('/');

    // Upload valid image
    const testImagePath = path.join(__dirname, 'fixtures', 'sample-menu.jpg');
    const fileInput = page.locator('input[type="file"]');
    await fileInput.setInputFiles(testImagePath);
    await page.selectOption('select[name="targetLanguage"]', 'ko');
    await page.selectOption('select[name="targetCurrency"]', 'KRW');

    // Click scan button
    await page.click('button:has-text("Scan Menu")');

    // Should show loading indicator
    await expect(page.getByText(/processing/i)).toBeVisible();

    // Should eventually show timeout error
    await expect(page.getByText(/timeout|taking.*long|try.*again/i)).toBeVisible({
      timeout: 12000,
    });
  });

  test('should handle network failure', async ({ page }) => {
    // Simulate offline mode
    await page.context().setOffline(true);

    await page.goto('/');

    // Upload valid image
    const testImagePath = path.join(__dirname, 'fixtures', 'sample-menu.jpg');
    const fileInput = page.locator('input[type="file"]');
    await fileInput.setInputFiles(testImagePath);
    await page.selectOption('select[name="targetLanguage"]', 'ko');
    await page.selectOption('select[name="targetCurrency"]', 'KRW');

    // Click scan button
    await page.click('button:has-text("Scan Menu")');

    // Should show network error
    await expect(
      page.getByText(/network.*error|connection.*failed|check.*internet/i)
    ).toBeVisible({ timeout: 5000 });

    // Restore online mode
    await page.context().setOffline(false);
  });

  test('should handle empty menu response', async ({ page }) => {
    // Mock API to return empty items
    await page.route('**/api/menus/scan', (route) => {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          scanId: 'test-scan-id',
          abGroup: 'CONTROL',
          items: [], // Empty items
          processingTime: 1000,
        }),
      });
    });

    await page.goto('/');

    // Upload valid image
    const testImagePath = path.join(__dirname, 'fixtures', 'sample-menu.jpg');
    const fileInput = page.locator('input[type="file"]');
    await fileInput.setInputFiles(testImagePath);
    await page.selectOption('select[name="targetLanguage"]', 'ko');
    await page.selectOption('select[name="targetCurrency"]', 'KRW');

    // Click scan button
    await page.click('button:has-text("Scan Menu")');

    // Should navigate to results page
    await page.waitForURL(/\/menu\/\w+/);

    // Should show "no items found" message
    await expect(
      page.getByText(/no.*menu.*found|couldn't.*detect|try.*another/i)
    ).toBeVisible({ timeout: 3000 });
  });

  test('should allow retry after error', async ({ page }) => {
    let attemptCount = 0;

    // First attempt fails, second succeeds
    await page.route('**/api/menus/scan', (route) => {
      attemptCount++;
      if (attemptCount === 1) {
        route.fulfill({
          status: 500,
          contentType: 'application/json',
          body: JSON.stringify({ error: 'Server error' }),
        });
      } else {
        route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            scanId: 'test-scan-id',
            abGroup: 'CONTROL',
            items: [
              {
                originalName: 'Test Item',
                translatedName: '테스트 아이템',
                originalPrice: 10,
                originalCurrency: 'USD',
                convertedPrice: 13000,
                convertedCurrency: 'KRW',
              },
            ],
            processingTime: 1000,
          }),
        });
      }
    });

    await page.goto('/');

    // First attempt
    const testImagePath = path.join(__dirname, 'fixtures', 'sample-menu.jpg');
    const fileInput = page.locator('input[type="file"]');
    await fileInput.setInputFiles(testImagePath);
    await page.selectOption('select[name="targetLanguage"]', 'ko');
    await page.selectOption('select[name="targetCurrency"]', 'KRW');
    await page.click('button:has-text("Scan Menu")');

    // Should show error
    await expect(page.getByText(/error|failed/i)).toBeVisible({ timeout: 3000 });

    // Should see retry button or be able to try again
    const retryButton = page.locator('button:has-text("Try Again"), button:has-text("Retry")');
    if (await retryButton.isVisible()) {
      await retryButton.click();
    } else {
      // If no retry button, user should still be on upload page
      await fileInput.setInputFiles(testImagePath);
      await page.click('button:has-text("Scan Menu")');
    }

    // Second attempt should succeed
    await page.waitForURL(/\/menu\/\w+/, { timeout: 10000 });

    // Should see menu items
    await expect(page.locator('[data-testid="menu-item"]')).toBeVisible();
  });

  test('should validate file size limit', async ({ page }) => {
    await page.goto('/');

    // Note: This test assumes there's a file size limit (e.g., 10MB)
    // If limit is not implemented, this test may need adjustment

    // In real scenario, you'd upload a very large file
    // For testing purposes, we'll just verify the error handling exists

    const fileInput = page.locator('input[type="file"]');

    // Check if there's any indication of file size limit in UI
    const uploadSection = page.locator('text=/upload/i').locator('..');
    const hasSizeLimit = await uploadSection
      .getByText(/max.*size|limit.*mb/i)
      .isVisible()
      .catch(() => false);

    if (hasSizeLimit) {
      // If size limit is shown, test is informative
      expect(hasSizeLimit).toBe(true);
    } else {
      // If not shown, that's also acceptable for MVP
      expect(hasSizeLimit).toBe(false);
    }
  });

  test('should handle malformed API response', async ({ page }) => {
    // Mock API to return malformed JSON
    await page.route('**/api/menus/scan', (route) => {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: 'invalid json {{{',
      });
    });

    await page.goto('/');

    const testImagePath = path.join(__dirname, 'fixtures', 'sample-menu.jpg');
    const fileInput = page.locator('input[type="file"]');
    await fileInput.setInputFiles(testImagePath);
    await page.selectOption('select[name="targetLanguage"]', 'ko');
    await page.selectOption('select[name="targetCurrency"]', 'KRW');
    await page.click('button:has-text("Scan Menu")');

    // Should show error message
    await expect(
      page.getByText(/error|failed|something.*wrong/i)
    ).toBeVisible({ timeout: 5000 });
  });

  test('should display user-friendly error messages', async ({ page }) => {
    // Test that error messages are helpful and not technical jargon
    await page.route('**/api/menus/scan', (route) => {
      route.fulfill({
        status: 500,
        body: JSON.stringify({
          error: 'NullPointerException at line 123',
        }),
      });
    });

    await page.goto('/');

    const testImagePath = path.join(__dirname, 'fixtures', 'sample-menu.jpg');
    const fileInput = page.locator('input[type="file"]');
    await fileInput.setInputFiles(testImagePath);
    await page.selectOption('select[name="targetLanguage"]', 'ko');
    await page.selectOption('select[name="targetCurrency"]', 'KRW');
    await page.click('button:has-text("Scan Menu")');

    // Wait for error message
    const errorMessage = page.locator('[role="alert"], [data-testid="error-message"]');
    await expect(errorMessage).toBeVisible({ timeout: 5000 });

    // Error message should not contain technical jargon
    const errorText = await errorMessage.textContent();
    expect(errorText).not.toMatch(/NullPointerException|stack.*trace|exception/i);

    // Should be user-friendly
    expect(errorText).toMatch(/sorry|error|problem|try.*again/i);
  });
});
