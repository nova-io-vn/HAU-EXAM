# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: critical.spec.js >> exam matrix generate flow reaches generated exam UI
- Location: e2e\critical.spec.js:10:1

# Error details

```
Test timeout of 30000ms exceeded.
```

```
Error: locator.fill: Test timeout of 30000ms exceeded.
Call log:
  - waiting for getByLabel(/tÃªn bá»™ Ä‘á»/i)

```

# Page snapshot

```yaml
- generic [ref=f1e2]:
  - generic [ref=f1e3]:
    - complementary "Điều hướng chính" [ref=f1e4]:
      - generic [ref=f1e5]:
        - generic [ref=f1e6]: H
        - generic [ref=f1e7]: HAU-EXAM
      - navigation [ref=f1e8]:
        - link "Dashboard" [ref=f1e9] [cursor=pointer]:
          - /url: /dashboard
          - generic [aria-hidden] [ref=f1e10]: D
        - link "Ngân hàng câu hỏi" [ref=f1e12] [cursor=pointer]:
          - /url: /questions
          - generic [aria-hidden] [ref=f1e13]: Q
        - link "Hàng đợi xét duyệt" [ref=f1e15] [cursor=pointer]:
          - /url: /review
          - generic [aria-hidden] [ref=f1e16]: V
        - link "Ma trận đề" [ref=f1e18] [cursor=pointer]:
          - /url: /exam-matrices
          - generic [aria-hidden] [ref=f1e19]: M
        - link "Bộ đề" [ref=f1e21] [cursor=pointer]:
          - /url: /exams
          - generic [aria-hidden] [ref=f1e22]: E
        - link "Không gian AI" [ref=f1e24] [cursor=pointer]:
          - /url: /ai/generate
          - generic [aria-hidden] [ref=f1e25]: A
        - link "Thông báo" [ref=f1e27] [cursor=pointer]:
          - /url: /notifications
          - generic [aria-hidden] [ref=f1e28]: "N"
      - paragraph [ref=f1e30]: SUBJECT_ADMIN
    - generic [ref=f1e31]:
      - banner [ref=f1e32]:
        - button "Thu gọn thanh bên" [ref=f1e33] [cursor=pointer]: ☰
        - generic [ref=f1e34]:
          - strong [ref=f1e35]: Sinh bộ đề
          - generic [ref=f1e36]: Đại học Kiến trúc Hà Nội
        - button "Thông báo, 1 chưa đọc" [ref=f1e38] [cursor=pointer]:
          - text: 🔔
          - generic [ref=f1e39]: "1"
        - group [ref=f1e40]:
          - generic "Mở menu tài khoản" [ref=f1e41] [cursor=pointer]:
            - generic [ref=f1e42]: E2
            - generic [ref=f1e43]:
              - strong [ref=f1e44]: E2E_SUBJECT_ADMIN
              - generic [ref=f1e45]: SUBJECT_ADMIN
            - generic [aria-hidden] [ref=f1e46]: ⌄
      - main [ref=f1e47]:
        - generic [ref=f1e48]:
          - generic [ref=f1e50]:
            - navigation "Breadcrumb" [ref=f1e51]:
              - list [ref=f1e52]:
                - listitem [ref=f1e53]:
                  - link "HAU-EXAM" [ref=f1e54] [cursor=pointer]:
                    - /url: /dashboard
                - listitem [ref=f1e55]: /Sinh bộ đề
            - heading "Sinh bộ đề" [level=1] [ref=f1e56]
            - paragraph [ref=f1e57]: Tạo phiên bản đầu tiên từ câu hỏi đã được duyệt theo ma trận.
          - group [ref=f1e59]:
            - generic [ref=f1e60]:
              - generic [ref=f1e61]: Tên bộ đề
              - textbox "Tên bộ đề" [ref=f1e62]
            - generic [ref=f1e63]:
              - generic [ref=f1e64]: Ma trận
              - combobox "Ma trận" [ref=f1e65]:
                - option "Chọn ma trận"
                - option "E2E Matrix · 1 câu" [selected]
            - paragraph [ref=f1e66]:
              - text: "Khoa:"
              - strong [ref=f1e67]: CNTT
              - text: · 1 câu ·
              - link "Xem phân bố" [ref=f1e68] [cursor=pointer]:
                - /url: /exam-matrices/00000000-0000-0000-0000-000000000050
            - generic [ref=f1e69]:
              - generic [ref=f1e70]: Template ID (UUID, không bắt buộc)
              - textbox "Template ID (UUID, không bắt buộc)" [ref=f1e71]
            - paragraph [ref=f1e72]: Nếu dùng template, template phải thuộc ma trận đã chọn.
            - button "Sinh bộ đề" [disabled] [ref=f1e73]
  - generic "Thông báo realtime"
```

# Test source

```ts
  1  | import {test,expect,ids} from './fixtures.js'
  2  | 
  3  | test('login restores an authenticated UI session',async({page,gateway})=>{await gateway.login('USER');await expect(page.getByText(/HAU-EXAM/).first()).toBeVisible()})
  4  | test('register reaches pending approval',async({page})=>{await page.goto('/register');await page.locator('#lecturerCode').fill('E2E_NEW_USER');await page.locator('#password').fill('test-password');await page.locator('main button').last().click();await expect(page).toHaveURL(/registration-pending/);await expect(page.getByRole('status')).toBeVisible()})
  5  | test('forgot password reaches OTP and reset screens',async({page})=>{await page.goto('/forgot-password');await page.locator('#lecturerCode').fill('E2E_USER');await page.locator('main button').last().click();await expect(page).toHaveURL(/verify-otp/);await page.locator('input[name="otp"]').fill('123456');await page.locator('main button').last().click();await expect(page).toHaveURL(/reset-password/);await page.locator('#password').fill('new-password');await page.locator('main button').last().click();await expect(page).toHaveURL(/login/)})
  6  | test('SYSTEM_ADMIN approves a pending registration',async({page,gateway})=>{await gateway.login('SYSTEM_ADMIN');await page.goto('/admin/registrations');await expect(page.getByText('E2E_USER').last()).toBeVisible();await page.locator('main button').first().click();await page.getByRole('button').last().click();await expect(page.getByText('E2E_USER').last()).toBeVisible()})
  7  | test('USER creates and submits a question, SUBJECT_ADMIN approves it',async({page,gateway})=>{await gateway.login('USER');await page.goto('/questions/new');await page.getByLabel(/question content/i).fill('E2E question content');await page.getByLabel('Subject').selectOption(ids.subject);await page.getByLabel('Chapter').selectOption(ids.chapter);for(const input of await page.getByLabel(/option content/i).all())await input.fill('Answer content');await page.getByRole('radio',{name:/^A/}).check();await page.locator('main button').last().click();await expect(page).toHaveURL(new RegExp(`/questions/${ids.question}`));await page.locator('main button').last().click();await page.getByRole('button').last().click();const adminPage=await page.context().newPage();await gateway.login('SUBJECT_ADMIN',adminPage);await adminPage.goto(`/review/${ids.question}`);await adminPage.locator('main button').last().click();await adminPage.getByRole('button').last().click();await expect(adminPage.getByRole('status')).toBeVisible()})
  8  | test('notification can be marked read through the UI',async({page,gateway})=>{await gateway.login('USER');await page.goto('/notifications');await expect(page.getByText('Question approved').first()).toBeVisible();await page.locator('main button').first().click();await expect(page.getByText(/0 chÆ°a Ä‘á»c/i)).toBeVisible()})
  9  | test('AI job UI uses deterministic processing fixture',async({page,gateway})=>{await gateway.login('USER');await page.goto(`/ai/jobs/${ids.job}`);await expect(page.getByText(/PROCESSING|COMPLETED|Äang xá»­ lÃ½/i)).toBeVisible();await page.reload();await expect(page.getByText(/COMPLETED|Generated question/i)).toBeVisible()})
> 10 | test('exam matrix generate flow reaches generated exam UI',async({page,gateway})=>{await gateway.login('SUBJECT_ADMIN');await page.goto('/exams/generate?matrixId='+ids.matrix);await page.getByLabel(/tÃªn bá»™ Ä‘á»/i).fill('E2E Exam');await page.locator('main button').last().click();await expect(page).toHaveURL(new RegExp(`/exams/${ids.exam}`))})
     |                                                                                                                                                                                                                           ^ Error: locator.fill: Test timeout of 30000ms exceeded.
  11 | 
```