# E2E Test Fixtures

This directory contains test fixtures for E2E tests.

## Required Files

### sample-menu.jpg
A sample menu image for testing the upload and scan flow.

**Requirements**:
- Format: JPEG
- Size: < 5MB
- Content: Any restaurant menu with text
- Recommended: Korean or English menu

**How to add**:
1. Find any restaurant menu photo
2. Save it as `sample-menu.jpg` in this directory
3. Ensure it's a valid image that can be processed by OCR

### invalid-file.txt
A text file for testing invalid file type handling.

**Status**: ✅ Automatically created

## Notes

- These fixtures are not committed to git (ignored via .gitignore)
- Developers must add their own `sample-menu.jpg` before running E2E tests
- You can use any menu image for testing purposes
