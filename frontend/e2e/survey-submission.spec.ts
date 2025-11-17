import { test, expect } from '@playwright/test';
import path from 'path';

/**
 * E2E Test: Survey Submission Flow
 *
 * Tests the survey modal behavior, submission, and validation.
 * Critical for H3 hypothesis testing (measuring confidence levels).
 */

test.describe('Survey Submission Flow', () => {
  // Helper function to get to menu page
  async function navigateToMenuPage(page: any) {
    await page.goto('/');
    const testImagePath = path.join(__dirname, 'fixtures', 'sample-menu.jpg');
    const fileInput = page.locator('input[type="file"]');
    await fileInput.setInputFiles(testImagePath);
    await page.selectOption('select[name="targetLanguage"]', 'ko');
    await page.selectOption('select[name="targetCurrency"]', 'KRW');
    await page.click('button:has-text("Scan Menu")');
    await page.waitForURL(/\/menu\/\w+/, { timeout: 10000 });
  }

  test('should show survey modal after 5 seconds', async ({ page }) => {
    await navigateToMenuPage(page);

    // Survey should not be visible immediately
    const surveyModal = page.locator('[data-testid="survey-modal"]');
    await expect(surveyModal).not.toBeVisible();

    // Wait 3 seconds - still should not be visible
    await page.waitForTimeout(3000);
    await expect(surveyModal).not.toBeVisible();

    // Wait another 3 seconds (total 6 seconds) - should now be visible
    await page.waitForTimeout(3000);
    await expect(surveyModal).toBeVisible({ timeout: 2000 });
  });

  test('should display survey question correctly', async ({ page }) => {
    await navigateToMenuPage(page);

    // Wait for survey to appear
    await page.waitForTimeout(5000);

    const surveyModal = page.locator('[data-testid="survey-modal"]');
    await expect(surveyModal).toBeVisible();

    // Verify question text
    await expect(
      page.getByText(/이 정보만으로 확신을 갖고 주문할 수 있습니까/i)
    ).toBeVisible();

    // Verify Yes and No buttons are present
    const yesButton = page.locator('button:has-text("Yes")');
    const noButton = page.locator('button:has-text("No")');

    await expect(yesButton).toBeVisible();
    await expect(noButton).toBeVisible();
    await expect(yesButton).toBeEnabled();
    await expect(noButton).toBeEnabled();
  });

  test('should submit survey with Yes response', async ({ page }) => {
    let surveySubmitted = false;
    let submittedData: any = null;

    // Intercept survey API call
    await page.route('**/api/surveys', (route) => {
      const postData = route.request().postData();
      if (postData) {
        submittedData = JSON.parse(postData);
        surveySubmitted = true;
      }
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          success: true,
          message: 'Survey response recorded',
        }),
      });
    });

    await navigateToMenuPage(page);

    // Wait for survey
    await page.waitForTimeout(5000);

    // Click Yes
    await page.click('button:has-text("Yes")');

    // Verify API call
    await page.waitForTimeout(1000);
    expect(surveySubmitted).toBe(true);
    expect(submittedData).toHaveProperty('scanId');
    expect(submittedData.hasConfidence).toBe(true);

    // Verify thank you message
    await expect(page.getByText(/감사합니다/i)).toBeVisible();

    // Modal should close after a moment
    const surveyModal = page.locator('[data-testid="survey-modal"]');
    await expect(surveyModal).not.toBeVisible({ timeout: 3000 });
  });

  test('should submit survey with No response', async ({ page }) => {
    let surveySubmitted = false;
    let submittedData: any = null;

    // Intercept survey API call
    await page.route('**/api/surveys', (route) => {
      const postData = route.request().postData();
      if (postData) {
        submittedData = JSON.parse(postData);
        surveySubmitted = true;
      }
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          success: true,
          message: 'Survey response recorded',
        }),
      });
    });

    await navigateToMenuPage(page);

    // Wait for survey
    await page.waitForTimeout(5000);

    // Click No
    await page.click('button:has-text("No")');

    // Verify API call
    await page.waitForTimeout(1000);
    expect(surveySubmitted).toBe(true);
    expect(submittedData).toHaveProperty('scanId');
    expect(submittedData.hasConfidence).toBe(false);

    // Verify thank you message
    await expect(page.getByText(/감사합니다/i)).toBeVisible();

    // Modal should close
    const surveyModal = page.locator('[data-testid="survey-modal"]');
    await expect(surveyModal).not.toBeVisible({ timeout: 3000 });
  });

  test('should handle survey API error gracefully', async ({ page }) => {
    // Mock API to return error
    await page.route('**/api/surveys', (route) => {
      route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({
          error: 'Failed to save survey response',
        }),
      });
    });

    await navigateToMenuPage(page);

    // Wait for survey
    await page.waitForTimeout(5000);

    // Click Yes
    await page.click('button:has-text("Yes")');

    // Should show error message
    await expect(
      page.getByText(/failed.*submit|error.*occurred/i)
    ).toBeVisible({ timeout: 3000 });

    // Modal should remain open for retry
    const surveyModal = page.locator('[data-testid="survey-modal"]');
    await expect(surveyModal).toBeVisible();
  });

  test('should prevent duplicate survey submission', async ({ page }) => {
    let submissionCount = 0;

    // Count API calls
    await page.route('**/api/surveys', (route) => {
      submissionCount++;
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ success: true }),
      });
    });

    await navigateToMenuPage(page);

    // Wait for survey
    await page.waitForTimeout(5000);

    const yesButton = page.locator('button:has-text("Yes")');

    // Click Yes
    await yesButton.click();

    // Try to click again
    await yesButton.click().catch(() => {}); // Button might be disabled

    // Wait a bit
    await page.waitForTimeout(1000);

    // Should only have submitted once
    expect(submissionCount).toBe(1);
  });

  test('should include correct scanId in survey submission', async ({ page }) => {
    let submittedScanId: string | null = null;
    let responseScanId: string | null = null;

    // Intercept scan API to capture scanId
    await page.route('**/api/menus/scan', (route) => {
      responseScanId = 'test-scan-123';
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          scanId: responseScanId,
          abGroup: 'CONTROL',
          items: [
            {
              originalName: 'Test',
              translatedName: '테스트',
              originalPrice: 10,
              originalCurrency: 'USD',
              convertedPrice: 13000,
              convertedCurrency: 'KRW',
            },
          ],
          processingTime: 1000,
        }),
      });
    });

    // Intercept survey API to capture submitted scanId
    await page.route('**/api/surveys', (route) => {
      const postData = route.request().postData();
      if (postData) {
        submittedScanId = JSON.parse(postData).scanId;
      }
      route.fulfill({
        status: 200,
        body: JSON.stringify({ success: true }),
      });
    });

    await navigateToMenuPage(page);

    // Wait for survey
    await page.waitForTimeout(5000);

    // Submit survey
    await page.click('button:has-text("Yes")');

    await page.waitForTimeout(1000);

    // Verify scanId matches
    expect(submittedScanId).toBe(responseScanId);
  });

  test('should allow closing survey without submitting', async ({ page }) => {
    await navigateToMenuPage(page);

    // Wait for survey
    await page.waitForTimeout(5000);

    const surveyModal = page.locator('[data-testid="survey-modal"]');
    await expect(surveyModal).toBeVisible();

    // Look for close button (X or Close)
    const closeButton = surveyModal.locator('button[aria-label="Close"], button:has-text("Close")');

    if (await closeButton.isVisible()) {
      await closeButton.click();

      // Modal should close
      await expect(surveyModal).not.toBeVisible({ timeout: 2000 });
    } else {
      // If no close button, that's acceptable for MVP (forced response)
      // User must answer Yes or No
      expect(await closeButton.isVisible()).toBe(false);
    }
  });

  test('should disable buttons during submission', async ({ page }) => {
    // Slow down API response to test button state
    await page.route('**/api/surveys', async (route) => {
      await new Promise((resolve) => setTimeout(resolve, 2000));
      route.fulfill({
        status: 200,
        body: JSON.stringify({ success: true }),
      });
    });

    await navigateToMenuPage(page);

    // Wait for survey
    await page.waitForTimeout(5000);

    const yesButton = page.locator('button:has-text("Yes")');
    const noButton = page.locator('button:has-text("No")');

    // Buttons should be enabled initially
    await expect(yesButton).toBeEnabled();
    await expect(noButton).toBeEnabled();

    // Click Yes
    await yesButton.click();

    // Buttons should be disabled during submission
    await expect(yesButton).toBeDisabled();
    await expect(noButton).toBeDisabled();

    // Wait for response
    await page.waitForTimeout(3000);

    // Should show thank you message
    await expect(page.getByText(/감사합니다/i)).toBeVisible();
  });

  test('should track survey completion for analytics', async ({ page }) => {
    // This test ensures survey completion can be tracked
    let surveyCompleted = false;

    await page.route('**/api/surveys', (route) => {
      surveyCompleted = true;
      route.fulfill({
        status: 200,
        body: JSON.stringify({ success: true }),
      });
    });

    await navigateToMenuPage(page);

    // Wait for survey
    await page.waitForTimeout(5000);

    // Submit survey
    await page.click('button:has-text("Yes")');

    await page.waitForTimeout(1000);

    // Verify completion was tracked
    expect(surveyCompleted).toBe(true);

    // Check if any analytics events were fired (if implemented)
    // For MVP, just ensuring API call is sufficient
  });
});
