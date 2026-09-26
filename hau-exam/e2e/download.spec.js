import { expect, test } from './fixtures.js'

const ANDROID_APK_URL = 'https://github.com/nova-io-vn/hau-exam-app/releases/latest/download/HAU-EXAM.apk'

test('download page is public and exposes the configured platform states', async ({ page }) => {
  const apiRequests = []
  page.on('request', (request) => {
    if (new URL(request.url()).pathname.startsWith('/api/v1/')) apiRequests.push(request.url())
  })

  await page.goto('/download')
  await page.reload()

  await expect(page).toHaveURL(/\/download$/)
  await expect(page).toHaveTitle('HAU-EXAM Mobile | Tải ứng dụng')
  await expect(page.locator('meta[name="description"]')).toHaveAttribute(
    'content',
    'Tải ứng dụng HAU-EXAM dành cho thiết bị di động.',
  )
  await expect(page.getByRole('heading', { name: 'HAU-EXAM trên thiết bị di động' })).toBeVisible()
  await expect(page.getByRole('link', { name: 'Tải xuống APK' }).first()).toHaveAttribute('href', ANDROID_APK_URL)
  await expect(page.getByRole('link', { name: 'Cài Expo Go' }).first()).toHaveAttribute('href', 'https://apps.apple.com/us/app/expo-go/id982107779')
  await expect(page.getByText('Tải ứng dụng trực tiếp')).toBeVisible()
  await expect(page.locator('a[href="' + ANDROID_APK_URL + '"]').first()).toBeVisible()
  await expect(page.locator('.public-page-transition')).toBeVisible()
  expect(apiRequests).toEqual([])
})

test('shared public header navigates between landing and download with active state', async ({ page }) => {
  await page.goto('/')

  const navigation = page.getByRole('navigation', { name: 'Điều hướng công khai' })
  const homeLink = navigation.getByRole('link', { name: 'Trang chủ' })
  const downloadLink = navigation.getByRole('link', { name: 'Tải ứng dụng' })

  await expect(homeLink).toHaveClass(/is-active/)
  await expect(homeLink).toHaveAttribute('aria-current', 'page')
  await downloadLink.click()
  await expect(page).toHaveURL(/\/download$/)
  await expect(downloadLink).toHaveClass(/is-active/)
  await expect(downloadLink).toHaveAttribute('aria-current', 'page')

  await homeLink.click()
  await expect(page).toHaveURL(/\/$/)
  await expect(page.getByRole('heading', { name: /Xây dựng ngân hàng câu hỏi/ })).toBeVisible()
})

test('installation guide switches between Android and iOS content', async ({ page }) => {
  await page.goto('/download')

  const androidTab = page.locator('#installation-tab-android')
  const iosTab = page.locator('#installation-tab-ios')

  await expect(androidTab).toHaveAttribute('aria-selected', 'true')
  await expect(page.getByRole('heading', { name: 'Tải ứng dụng', exact: true }).last()).toBeVisible()
  await expect(page.locator('#download-detail-panel-android')).toBeVisible()
  await page.locator('.download-card-ios').click()
  await expect(page.locator('#download-detail-panel-ios')).toBeVisible()
  await expect(page.locator('img[src="/assets/qr.png"]')).toBeVisible()
  await expect(page.getByText('Quét mã bằng Expo Go để trải nghiệm HAU-EXAM')).toBeVisible()
  await expect(page.getByRole('link', { name: 'Cài Expo Go' }).last()).toHaveAttribute('href', 'https://apps.apple.com/us/app/expo-go/id982107779')
  await iosTab.click()
  await expect(iosTab).toHaveAttribute('aria-selected', 'true')
  await expect(page.getByText('Quét QR bằng Expo Go để tải và mở project HAU-EXAM.')).toBeVisible()
  await expect(page.getByRole('link', { name: 'Cài Expo Go' }).last()).toBeVisible()
  await androidTab.click()
  await expect(page.getByRole('heading', { name: 'Chọn Cài đặt' })).toBeVisible()
})

test('download page follows the existing dark theme preference', async ({ page }) => {
  await page.addInitScript(() => localStorage.setItem('hau-qm-theme', 'dark'))
  await page.goto('/download')

  await expect(page.locator('html')).toHaveAttribute('data-theme', 'dark')
  const colors = await page.locator('.download-page').evaluate((element) => {
    const pageStyle = getComputedStyle(element)
    const cardStyle = getComputedStyle(document.querySelector('.download-card'))
    return { page: pageStyle.backgroundColor, card: cardStyle.backgroundColor }
  })
  expect(colors.page).not.toBe('rgb(251, 248, 248)')
  expect(colors.card).not.toBe('rgb(255, 255, 255)')
})

test('theme preference is controlled from the account menu and persists after refresh', async ({ page, gateway }) => {
  await gateway.login('USER')
  await page.getByLabel('Mở menu tài khoản').click()
  await page.getByRole('button', { name: 'Chuyển sang giao diện tối' }).click()
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'dark')

  await page.reload()
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'dark')
  await page.getByLabel('Mở menu tài khoản').click()
  await expect(page.getByRole('button', { name: 'Chuyển sang giao diện sáng' })).toBeVisible()
})

for (const width of [375, 768, 1440]) {
  test(`public download experience remains responsive at ${width}px`, async ({ page }) => {
    await page.setViewportSize({ width, height: 900 })
    await page.goto('/download')

    await expect(page.getByRole('heading', { name: 'Tải ứng dụng', exact: true }).first()).toBeVisible()
    const hasHorizontalOverflow = await page.evaluate(
      () => document.documentElement.scrollWidth > document.documentElement.clientWidth,
    )
    expect(hasHorizontalOverflow).toBe(false)

    const cards = page.locator('.download-card')
    const androidCard = await cards.nth(0).boundingBox()
    const iosCard = await cards.nth(1).boundingBox()
    expect(androidCard).not.toBeNull()
    expect(iosCard).not.toBeNull()

    if (width === 375) {
      expect(iosCard.y).toBeGreaterThan(androidCard.y + androidCard.height)
      await page.getByLabel('Mở menu điều hướng').click()
      await expect(page.getByRole('navigation', { name: 'Điều hướng công khai' })).toBeVisible()
      await expect(page.getByRole('link', { name: 'Bắt đầu sử dụng' })).toHaveCount(0)
    } else {
      expect(Math.abs(iosCard.y - androidCard.y)).toBeLessThan(2)
    }
  })
}
