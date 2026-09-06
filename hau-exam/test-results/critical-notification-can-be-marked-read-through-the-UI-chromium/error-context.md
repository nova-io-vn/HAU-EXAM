# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: critical.spec.js >> notification can be marked read through the UI
- Location: e2e\critical.spec.js:8:1

# Error details

```
Error: expect(locator).toBeVisible() failed

Locator: getByText(/0 chÆ°a Ä‘á»c/i)
Expected: visible
Timeout: 5000ms
Error: element(s) not found

Call log:
  - Expect "toBeVisible" getByText(/0 chÆ°a Ä‘á»c/i) with timeout 5000ms
  - waiting for getByText(/0 chÆ°a Ä‘á»c/i)

```

```yaml
- complementary "Điều hướng chính":
  - text: H HAU-EXAM
  - navigation:
    - link "Dashboard":
      - /url: /dashboard
    - link "Câu hỏi của tôi":
      - /url: /questions/mine
    - link "Tạo câu hỏi":
      - /url: /questions/new
    - link "Tạo sinh AI":
      - /url: /ai/generate
    - link "Tài liệu":
      - /url: /ai/documents
    - link "Chatbot":
      - /url: /chat
    - link "Thông báo":
      - /url: /notifications
  - paragraph: USER
- banner:
  - button "Thu gọn thanh bên": ☰
  - strong: Thông báo
  - text: Đại học Kiến trúc Hà Nội
  - button "Thông báo, 0 chưa đọc": 🔔
  - group:
    - text: E2
    - strong: E2E_USER
    - text: USER
- main:
  - navigation "Breadcrumb":
    - list:
      - listitem:
        - link "HAU-EXAM":
          - /url: /dashboard
      - listitem: /Thông báo
  - heading "Thông báo" [level=1]
  - paragraph: Lịch sử thông báo được đồng bộ từ dữ liệu bền vững của Notification Service.
  - text: Realtime gián đoạn · REST vẫn khả dụng 0 chưa đọc
  - article:
    - 'button "Đã đọc: Question approved"':
      - strong: QUESTION APPROVED
      - time: 07:00 1/1/26
      - text: Question approved Your question was approved.
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
> 8  | test('notification can be marked read through the UI',async({page,gateway})=>{await gateway.login('USER');await page.goto('/notifications');await expect(page.getByText('Question approved').first()).toBeVisible();await page.locator('main button').first().click();await expect(page.getByText(/0 chÆ°a Ä‘á»c/i)).toBeVisible()})
     |                                                                                                                                                                                                                                                                                                                       ^ Error: expect(locator).toBeVisible() failed
  9  | test('AI job UI uses deterministic processing fixture',async({page,gateway})=>{await gateway.login('USER');await page.goto(`/ai/jobs/${ids.job}`);await expect(page.getByText(/PROCESSING|COMPLETED|Äang xá»­ lÃ½/i)).toBeVisible();await page.reload();await expect(page.getByText(/COMPLETED|Generated question/i)).toBeVisible()})
  10 | test('exam matrix generate flow reaches generated exam UI',async({page,gateway})=>{await gateway.login('SUBJECT_ADMIN');await page.goto('/exams/generate?matrixId='+ids.matrix);await page.getByLabel(/tÃªn bá»™ Ä‘á»/i).fill('E2E Exam');await page.locator('main button').last().click();await expect(page).toHaveURL(new RegExp(`/exams/${ids.exam}`))})
  11 | 
```