import { test, expect, type Page, type ConsoleMessage } from '@playwright/test';

const BASE = 'https://eventhub.irflab.tech';

// Collect JS errors and console errors for debugging
async function collectErrors(page: Page) {
  const jsErrors: string[] = [];
  const networkFails: string[] = [];

  page.on('console', (msg: ConsoleMessage) => {
    if (msg.type() === 'error') jsErrors.push(msg.text());
  });
  page.on('pageerror', (err: Error) => jsErrors.push(err.message));
  page.on('requestfailed', (req) =>
    networkFails.push(`${req.method()} ${req.url()} → ${req.failure()?.errorText}`)
  );

  return { jsErrors, networkFails };
}

test.describe('EventHub — public pages', () => {
  test('home page loads', async ({ page }) => {
    const { jsErrors } = await collectErrors(page);
    await page.goto('/');
    await expect(page).toHaveTitle(/EventHub/i);
    await page.waitForLoadState('networkidle');
    expect(jsErrors, `JS errors on home: ${jsErrors.join('\n')}`).toHaveLength(0);
  });

  test('events page loads and shows events or empty state', async ({ page }) => {
    const { jsErrors, networkFails } = await collectErrors(page);
    await page.goto('/events');
    await page.waitForLoadState('networkidle');

    // Should NOT show the browser's "couldn't load" error
    const bodyText = await page.locator('body').innerText();
    expect(bodyText).not.toContain("This page couldn't load");

    // Must render the page heading
    await expect(page.getByRole('heading', { name: /upcoming events/i })).toBeVisible();

    // Either a card or empty-state is shown (not a crash)
    const hasEvents = await page.locator('a[href^="/events/"]').count() > 0;
    const hasEmpty  = await page.getByText(/no events found/i).isVisible().catch(() => false);
    const hasError  = await page.locator('.errorBox').isVisible().catch(() => false);

    expect(hasEvents || hasEmpty || hasError,
      'Events page rendered no recognisable state').toBeTruthy();

    // Surface any hidden JS errors
    if (jsErrors.length) console.warn('JS errors:', jsErrors);
    if (networkFails.length) console.warn('Network fails:', networkFails);
  });

  test('event detail page loads', async ({ page }) => {
    // First get a real event id from the API
    const res = await page.request.get(`${BASE}/api/backend/events`);
    expect(res.ok(), `Events API returned ${res.status()}`).toBeTruthy();
    const body = await res.json() as { content?: { id: number }[]; id?: number }[];

    // Support both paginated and array responses
    const events = Array.isArray(body)
      ? body
      : (body as unknown as { content: { id: number }[] }).content ?? [];

    test.skip(events.length === 0, 'No events to test with');

    const firstId = (events[0] as { id: number }).id;
    const { jsErrors } = await collectErrors(page);
    await page.goto(`/events/${firstId}`);
    await page.waitForLoadState('networkidle');

    await expect(page.getByRole('heading').first()).toBeVisible();
    expect(jsErrors, `JS errors on event detail: ${jsErrors.join('\n')}`).toHaveLength(0);
  });

  test('login page renders', async ({ page }) => {
    await page.goto('/login');
    await expect(page.getByRole('heading', { name: /sign in|log in/i })).toBeVisible();
  });

  test('register page renders', async ({ page }) => {
    await page.goto('/register');
    await expect(page.getByRole('heading', { name: /create account|register|sign up/i })).toBeVisible();
  });
});

test.describe('EventHub — API rewrite sanity', () => {
  test('GET /api/backend/events is public and returns data', async ({ request }) => {
    const res = await request.get(`${BASE}/api/backend/events`);
    expect(res.status()).toBe(200);
    const body = await res.text();
    expect(body).toContain('{');
  });

  test('POST /api/backend/bookings requires auth (401)', async ({ request }) => {
    const res = await request.post(`${BASE}/api/backend/bookings`, {
      data: { eventId: 1, items: [] },
    });
    expect(res.status()).toBe(401);
  });
});
