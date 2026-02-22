import { test, expect } from '@playwright/test';

test('has title', async ({ page }) => {
  await page.goto('/auth');
  await expect(page).toHaveTitle(/GraphFlix/i);
});

test('login success shows toast and redirects', async ({ page }) => {
  page.on('console', msg => console.log(msg.text()));
  page.on('pageerror', exception => console.log(`Uncaught exception: "${exception}"`));

  await page.goto('/auth');
  
  // Wait for form to be visible
  await page.waitForSelector('form', { state: 'visible' });

  // Use ID selectors
  await page.fill('#email', 'test@example.com');
  await page.fill('#password', 'password12345');
  
  await page.click('button[type="submit"]');
  
  // Check for Angular Material Snackbar
  const snackbar = page.locator('.mat-mdc-snack-bar-container');
  await expect(snackbar).toBeVisible({ timeout: 10000 });
  await expect(snackbar).toContainText('Login successful!');
  
  // Check redirect
  await expect(page).toHaveURL('/');
});

test('login failure shows error toast', async ({ page }) => {
  page.on('console', msg => console.log(msg.text()));
  page.on('pageerror', exception => console.log(`Uncaught exception: "${exception}"`));

  await page.goto('/auth');
  
  await page.waitForSelector('form', { state: 'visible' });

  await page.fill('#email', 'wrong@example.com');
  await page.fill('#password', 'wrongpass123');
  
  await page.click('button[type="submit"]');
  
  const snackbar = page.locator('.mat-mdc-snack-bar-container');
  await expect(snackbar).toBeVisible({ timeout: 10000 });
  await expect(snackbar).toHaveClass(/toast-error/);
});
