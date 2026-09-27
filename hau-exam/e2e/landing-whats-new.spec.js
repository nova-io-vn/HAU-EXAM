import { test, expect } from './fixtures.js'

const landingViewports = [
  { width: 390, height: 844, columns: 1 },
  { width: 768, height: 1024, columns: 2 },
  { width: 1366, height: 768, columns: 3 },
  { width: 1440, height: 900, columns: 3 },
  { width: 1920, height: 1080, columns: 3 },
]

async function noHorizontalOverflow(page) {
  await expect.poll(() => page.evaluate(() => document.documentElement.scrollWidth - document.documentElement.clientWidth)).toBeLessThanOrEqual(1)
}

for (const viewport of landingViewports) {
  test(`landing visual contract at ${viewport.width}x${viewport.height}`, async ({ page }) => {
    await page.setViewportSize(viewport)
    await page.goto('/')
    await noHorizontalOverflow(page)
    await expect(page.locator('.public-feature-grid article')).toHaveCount(6)
    const cards = await page.locator('.public-feature-grid article').evaluateAll((items) => items.map((item) => Math.round(item.getBoundingClientRect().top)))
    expect(new Set(cards.slice(0, viewport.columns)).size).toBe(1)
    if (viewport.columns > 1) expect(cards[viewport.columns]).toBeGreaterThan(cards[0])
    await expect(page.locator('.landing-workflow-steps button')).toHaveCount(4)
    await page.locator('.landing-workflow-steps button').nth(2).click()
    await expect(page.locator('.landing-workflow-visual figcaption')).toContainText('Bước 3')
    const cta = page.locator('.public-cta')
    await expect(cta).toBeVisible()
    const ctaStyle = await cta.evaluate((element) => {
      const style = getComputedStyle(element)
      return { backgroundImage: style.backgroundImage, color: style.color }
    })
    expect(ctaStyle.backgroundImage).toContain('linear-gradient')
    expect(ctaStyle.color).toBe('rgb(255, 255, 255)')
  })
}

test('what is new is versioned and starts the real role-aware tour', async ({ page, gateway }) => {
  await page.setViewportSize({ width: 390, height: 844 })
  await gateway.login('USER')
  const dialog = page.getByRole('dialog', { name: /MỚI TRÊN HAU EXAM/i })
  await expect(dialog).toBeVisible()
  await dialog.getByRole('button', { name: 'Bỏ qua' }).click()
  await expect(dialog).toBeHidden()
  await page.reload()
  await expect(dialog).toBeHidden()

  await page.evaluate(() => localStorage.setItem('hau-qm:whats-new:00000000-0000-0000-0000-000000000001', 'older-version'))
  await page.reload()
  await expect(dialog).toBeVisible()
  await noHorizontalOverflow(page)
  await dialog.getByLabel('Không hiển thị lại thông báo này').check()
  await dialog.locator('.dialog-head button').click()
  await page.reload()
  await expect(dialog).toBeHidden()

  await page.evaluate(() => localStorage.setItem('hau-qm:whats-new:00000000-0000-0000-0000-000000000001', 'older-version'))
  await page.reload()
  await expect(dialog).toBeVisible()
  await dialog.getByRole('button', { name: 'Bắt đầu hướng dẫn' }).click()
  await expect(page.locator('.onboarding-controls')).toBeVisible()
  await expect(page.locator('.onboarding-controls')).toContainText('1 /')
})

test('workflow does not auto-advance when reduced motion is requested', async ({ page }) => {
  await page.emulateMedia({ reducedMotion: 'reduce' })
  await page.goto('/')
  const selected = page.locator('.landing-workflow-steps button[aria-selected="true"]')
  await expect(selected).toContainText('Tạo câu hỏi')
  await page.waitForTimeout(5200)
  await expect(selected).toContainText('Tạo câu hỏi')
})
