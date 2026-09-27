import { test, expect, ids } from './fixtures.js'

test('question creation uses a compact authoring workspace and route-aware identity', async ({ page, gateway }) => {
  await gateway.login('USER')
  await page.goto('/questions/new')

  const formattedDate = new Intl.DateTimeFormat('vi-VN', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' }).format(new Date())
  const expectedDate = formattedDate.charAt(0).toUpperCase() + formattedDate.slice(1)
  await expect(page.locator('.topbar-date')).toHaveText(expectedDate)
  await expect(page).toHaveTitle('Tạo câu hỏi | HAU QM')
  await expect(page.getByRole('navigation', { name: 'Breadcrumb' })).toContainText('Ngân hàng câu hỏi')
  await expect(page.getByRole('navigation', { name: 'Breadcrumb' })).toContainText('Tạo câu hỏi')
  await expect(page.locator('.topbar')).not.toContainText('Tạo câu hỏi')

  for (const label of ['In đậm', 'In nghiêng', 'Gạch chân', 'Danh sách', 'Danh sách đánh số', 'Hoàn tác', 'Làm lại']) {
    await expect(page.getByRole('button', { name: label, exact: true })).toBeVisible()
  }
  await expect(page.getByLabel('Cỡ chữ')).toHaveCount(0)
  await expect(page.getByRole('button', { name: 'Thêm ảnh cho câu hỏi' })).toBeVisible()
  await expect(page.locator('.image-picker-preview')).toHaveCount(0)
  await expect(page.locator('.option-row')).toHaveCount(4)
  await expect(page.getByLabel('Nội dung phương án A')).toHaveAttribute('placeholder', 'Nhập nội dung phương án A...')
  await expect(page.getByRole('button', { name: 'Thêm ảnh cho phương án A' })).toBeVisible()
})

test('question workspace remains usable at tablet and desktop widths', async ({ page, gateway }, testInfo) => {
  await gateway.login('USER')
  for (const width of [1024, 1280, 1366, 1440, 1920]) {
    await page.setViewportSize({ width, height: 900 })
    await page.goto('/questions/new')
    const overflow = await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth)
    expect(overflow).toBe(false)
    await expect(page.locator('.option-row').first()).toBeVisible()
    if (width === 1366) await page.screenshot({ path: testInfo.outputPath('question-workspace-light.png'), fullPage: true })
  }
  await page.evaluate(() => localStorage.setItem('hau-qm-theme', 'dark'))
  await page.setViewportSize({ width: 1366, height: 900 })
  await page.reload()
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'dark')
  await expect(page.locator('.question-workspace')).toBeVisible()
  await page.screenshot({ path: testInfo.outputPath('question-workspace-dark.png'), fullPage: true })
})

test('question image is compact and edit workspace preserves editable content', async ({ page, gateway }) => {
  await gateway.login('USER')
  await page.goto('/questions/new')
  await page.locator('.is-question-image input[type="file"]').setInputFiles({
    name: 'question.png',
    mimeType: 'image/png',
    buffer: Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=', 'base64'),
  })
  await expect(page.locator('.is-question-image .image-preview-frame')).toBeVisible()
  await expect(page.getByRole('button', { name: 'Thay ảnh' }).first()).toBeVisible()
  await page.getByRole('button', { name: 'Xóa' }).first().click()
  await expect(page.locator('.is-question-image .image-preview-frame')).toHaveCount(0)

  await page.goto(`/questions/${ids.question}/edit`)
  await expect(page).toHaveTitle('Chỉnh sửa câu hỏi | HAU QM')
  await expect(page.getByLabel('Nội dung câu hỏi')).toContainText('E2E question content')
  await page.getByLabel('Nội dung câu hỏi').fill('Edited E2E question content')
  await page.getByRole('button', { name: 'Lưu thay đổi' }).click()
  await expect(page).toHaveURL(new RegExp(`/questions/${ids.question}$`))
})

test('profile uses localized breadcrumb and branding-aware document title', async ({ page, gateway }) => {
  await gateway.login('USER')
  await page.goto('/profile')
  await expect(page).toHaveTitle('Hồ sơ cá nhân | HAU QM')
  await expect(page.getByRole('navigation', { name: 'Breadcrumb' })).toContainText('Tài khoản')
  await expect(page.getByRole('navigation', { name: 'Breadcrumb' })).toContainText('Hồ sơ cá nhân')
})
