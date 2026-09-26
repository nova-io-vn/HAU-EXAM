import { test, expect } from './fixtures.js'

const counts = { APPROVED: 120, PENDING_REVIEW: 35, NEED_REVISION: 18, DRAFT: 22, REJECTED: 8, ARCHIVED: 7 }

async function questionCounts(context, values = counts) {
  await context.route('**/api/v1/questions?*', route => {
    const status = new URL(route.request().url()).searchParams.get('status')
    const totalElements = status ? values[status] ?? 0 : Object.values(values).reduce((sum, value) => sum + value, 0)
    return route.fulfill({ json: { success: true, code: 'SUCCESS', data: { items: [], page: 0, totalPages: Math.ceil(totalElements / 5), totalElements } } })
  })
}

for (const role of ['USER', 'SUBJECT_ADMIN']) {
  test(`${role} shows the complete question distribution at desktop and phone widths`, async ({ page, context, gateway }, testInfo) => {
    await questionCounts(context)
    await page.setViewportSize({ width: 1440, height: 1000 })
    await gateway.login(role)
    const chart = page.locator('.question-status-chart')
    await expect(chart).toHaveAccessibleName('Phân bố trạng thái câu hỏi, tổng 210 câu hỏi')
    await expect(chart.locator('.chart-summary > div')).toHaveCount(6)
    await expect(chart.locator('.chart-summary')).toContainText('Đã lưu trữ')
    await expect(chart.locator('.chart-summary')).toContainText('Đã từ chối')
    await expect(page.locator('.page-header h1')).toHaveCSS('color', 'rgb(0, 0, 0)')
    await expect(page.locator('.page-actions .button-primary')).toHaveCSS('background-color', 'rgb(0, 51, 160)')

    for (const width of [1440, 1024, 390, 320]) {
      await page.setViewportSize({ width, height: width < 768 ? 844 : 1000 })
      await expect(page.locator(width >= 1200 ? '.desktop-toggle' : '.mobile-toggle')).toBeVisible()
      await expect(page.locator(width >= 1200 ? '.mobile-toggle' : '.desktop-toggle')).toBeHidden()
      await expect(chart.locator('.recharts-pie-sector')).toHaveCount(6)
      await expect.poll(async () => {
        const bounds = await chart.locator('.chart-visual').boundingBox()
        const center = await chart.locator('.chart-center').boundingBox()
        return Math.abs((bounds.y + bounds.height / 2) - (center.y + center.height / 2))
      }).toBeLessThan(1)
      const chartBounds = await chart.boundingBox()
      expect(chartBounds.x).toBeGreaterThanOrEqual(0)
      expect(chartBounds.x + chartBounds.width).toBeLessThanOrEqual(width)
      await expect.poll(() => chart.evaluate(el => el.scrollWidth - el.clientWidth)).toBeLessThanOrEqual(1)
      await page.screenshot({ path: testInfo.outputPath(`dashboard-${role}-${width}.png`), fullPage: true })
    }
  })
}

test('empty distribution has a readable empty state and no ring', async ({ page, context, gateway }) => {
  await questionCounts(context, {})
  await gateway.login('USER')
  await expect(page.locator('.chart-empty')).toContainText('Chưa có dữ liệu')
  await expect(page.locator('.recharts-pie')).toHaveCount(0)
})

test('a single archived state fills the chart and dark mode remains readable', async ({ page, context, gateway }, testInfo) => {
  await context.addInitScript(() => localStorage.setItem('hau-qm-theme', 'dark'))
  await questionCounts(context, { ARCHIVED: 5 })
  await gateway.login('USER')
  await expect(page.locator('.chart-summary')).toContainText('100%')
  await expect(page.locator('.recharts-pie-sector')).toHaveCount(1)
  await expect(page.locator('.page-header h1')).toHaveCSS('color', 'rgb(245, 247, 251)')
  await page.screenshot({ path: testInfo.outputPath('dashboard-dark.png'), fullPage: true })
})

test('dashboard recovers after a failed count request', async ({ page, context, gateway }) => {
  let failed = true
  await questionCounts(context)
  await context.route('**/api/v1/questions?*', route => failed
    ? route.fulfill({ status: 503, json: { success: false, code: 'UNAVAILABLE', message: 'Không thể tải dữ liệu' } })
    : route.fallback())
  await gateway.login('USER')
  await expect(page.locator('.admin-error')).toBeVisible()
  failed = false
  await page.getByRole('button', { name: 'Thử lại', exact: true }).click()
  await expect(page.locator('.chart-center strong')).toHaveText('210')
})

test('system dashboard shows real metrics without unfinished placeholder panels', async ({ page, gateway }, testInfo) => {
  await page.setViewportSize({ width: 1440, height: 1000 })
  await gateway.login('SYSTEM_ADMIN')
  await expect(page.getByRole('heading', { name: 'Tài khoản chờ phê duyệt' })).toBeVisible()
  await expect(page.getByText('Chưa có Activity API')).toHaveCount(0)
  await expect(page.locator('.traffic-card')).toHaveCount(3)
  await page.screenshot({ path: testInfo.outputPath('dashboard-admin.png'), fullPage: true })
})
