# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: critical.spec.js >> USER creates and submits a question, SUBJECT_ADMIN approves it
- Location: e2e\critical.spec.js:7:1

# Error details

```
Test timeout of 30000ms exceeded.
```

```
Error: locator.click: Test timeout of 30000ms exceeded.
Call log:
  - waiting for locator('main button').last()
    - locator resolved to <button type="button" aria-label="Đóng">×</button>
  - attempting click action
    2 × waiting for element to be visible, enabled and stable
      - element is not visible
    - retrying click action
    - waiting 20ms
    2 × waiting for element to be visible, enabled and stable
      - element is not visible
    - retrying click action
      - waiting 100ms
    57 × waiting for element to be visible, enabled and stable
       - element is not visible
     - retrying click action
       - waiting 500ms

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
        - link "Câu hỏi của tôi" [ref=f1e12] [cursor=pointer]:
          - /url: /questions/mine
          - generic [aria-hidden] [ref=f1e13]: Q
        - link "Tạo câu hỏi" [ref=f1e15] [cursor=pointer]:
          - /url: /questions/new
          - generic [aria-hidden] [ref=f1e16]: +
        - link "Tạo sinh AI" [ref=f1e18] [cursor=pointer]:
          - /url: /ai/generate
          - generic [aria-hidden] [ref=f1e19]: A
        - link "Tài liệu" [ref=f1e21] [cursor=pointer]:
          - /url: /ai/documents
          - generic [aria-hidden] [ref=f1e22]: D
        - link "Chatbot" [ref=f1e24] [cursor=pointer]:
          - /url: /chat
          - generic [aria-hidden] [ref=f1e25]: C
        - link "Thông báo" [ref=f1e27] [cursor=pointer]:
          - /url: /notifications
          - generic [aria-hidden] [ref=f1e28]: "N"
      - paragraph [ref=f1e30]: USER
    - generic [ref=f1e31]:
      - banner [ref=f1e32]:
        - button "Thu gọn thanh bên" [ref=f1e33] [cursor=pointer]: ☰
        - generic [ref=f1e34]:
          - strong [ref=f1e35]: Tạo câu hỏi
          - generic [ref=f1e36]: Đại học Kiến trúc Hà Nội
        - button "Thông báo, 1 chưa đọc" [ref=f1e38] [cursor=pointer]:
          - text: 🔔
          - generic [ref=f1e39]: "1"
        - group [ref=f1e40]:
          - generic "Mở menu tài khoản" [ref=f1e41] [cursor=pointer]:
            - generic [ref=f1e42]: E2
            - generic [ref=f1e43]:
              - strong [ref=f1e44]: E2E_USER
              - generic [ref=f1e45]: USER
            - generic [aria-hidden] [ref=f1e46]: ⌄
      - main [ref=f1e47]:
        - generic [ref=f1e48]:
          - generic [ref=f1e50]:
            - navigation "Breadcrumb" [ref=f1e51]:
              - list [ref=f1e52]:
                - listitem [ref=f1e53]:
                  - link "HAU-EXAM" [ref=f1e54] [cursor=pointer]:
                    - /url: /dashboard
                - listitem [ref=f1e55]: /Tạo câu hỏi
            - heading "Tạo câu hỏi" [level=1] [ref=f1e56]
            - paragraph [ref=f1e57]: Tạo câu hỏi thủ công ở trạng thái do Question Service quyết định.
          - generic [ref=f1e58]:
            - group [ref=f1e59]:
              - generic [ref=f1e60]:
                - heading "Nội dung câu hỏi" [level=2] [ref=f1e61]
                - generic [ref=f1e62]:
                  - generic [ref=f1e63]: Question Content
                  - textbox "Question Content" [ref=f1e64]: E2E question content
                - generic [ref=f1e65]:
                  - generic [ref=f1e66]: Question Image URL (không bắt buộc)
                  - textbox "Question Image URL (không bắt buộc)" [ref=f1e67]
              - generic [ref=f1e68]:
                - heading "Cấu hình" [level=2] [ref=f1e69]
                - generic [ref=f1e70]:
                  - generic [ref=f1e71]:
                    - generic [ref=f1e72]: Khoa
                    - textbox "Khoa" [ref=f1e73]: CNTT
                  - generic [ref=f1e74]:
                    - generic [ref=f1e75]: Question Type
                    - combobox "Question Type" [ref=f1e76]:
                      - option "SINGLE CHOICE" [selected]
                      - option "MULTIPLE CHOICE"
                      - option "TRUE FALSE"
                  - generic [ref=f1e77]:
                    - generic [ref=f1e78]: Difficulty
                    - combobox "Difficulty" [ref=f1e79]:
                      - option "EASY"
                      - option "MEDIUM" [selected]
                      - option "HARD"
                  - generic [ref=f1e80]:
                    - generic [ref=f1e81]: Subject
                    - combobox "Subject" [ref=f1e82]:
                      - option "Chọn môn học"
                      - option "E2E Subject" [selected]
                  - generic [ref=f1e83]:
                    - generic [ref=f1e84]: Chapter
                    - combobox "Chapter" [ref=f1e85]:
                      - option "Không chọn"
                      - option "E2E Chapter" [selected]
                  - generic [ref=f1e86]:
                    - generic [ref=f1e87]: Topic
                    - combobox "Topic" [ref=f1e88]:
                      - option "Không chọn" [selected]
                      - option "E2E Topic"
              - generic [ref=f1e89]:
                - heading "Phương án trả lời" [level=2] [ref=f1e90]
                - generic [ref=f1e91]:
                  - article [ref=f1e92]:
                    - generic [ref=f1e93] [cursor=pointer]:
                      - radio "A Đáp án đúng" [checked] [active] [ref=f1e94]
                      - strong [ref=f1e95]: A
                      - generic [ref=f1e96]: Đáp án đúng
                    - generic [ref=f1e97]:
                      - generic [ref=f1e98]: Nội dung phương án
                      - textbox "Nội dung phương án" [ref=f1e99]
                    - generic [ref=f1e100]:
                      - generic [ref=f1e101]: Image URL (không bắt buộc)
                      - textbox "Image URL (không bắt buộc)" [ref=f1e102]
                    - button "Xóa phương án A" [ref=f1e103] [cursor=pointer]
                  - article [ref=f1e104]:
                    - generic [ref=f1e105] [cursor=pointer]:
                      - radio "B Đáp án đúng" [ref=f1e106]
                      - strong [ref=f1e107]: B
                      - generic [ref=f1e108]: Đáp án đúng
                    - generic [ref=f1e109]:
                      - generic [ref=f1e110]: Nội dung phương án
                      - textbox "Nội dung phương án" [ref=f1e111]
                    - generic [ref=f1e112]:
                      - generic [ref=f1e113]: Image URL (không bắt buộc)
                      - textbox "Image URL (không bắt buộc)" [ref=f1e114]
                    - button "Xóa phương án B" [ref=f1e115] [cursor=pointer]
                  - article [ref=f1e116]:
                    - generic [ref=f1e117] [cursor=pointer]:
                      - radio "C Đáp án đúng" [ref=f1e118]
                      - strong [ref=f1e119]: C
                      - generic [ref=f1e120]: Đáp án đúng
                    - generic [ref=f1e121]:
                      - generic [ref=f1e122]: Nội dung phương án
                      - textbox "Nội dung phương án" [ref=f1e123]
                    - generic [ref=f1e124]:
                      - generic [ref=f1e125]: Image URL (không bắt buộc)
                      - textbox "Image URL (không bắt buộc)" [ref=f1e126]
                    - button "Xóa phương án C" [ref=f1e127] [cursor=pointer]
                  - article [ref=f1e128]:
                    - generic [ref=f1e129] [cursor=pointer]:
                      - radio "D Đáp án đúng" [ref=f1e130]
                      - strong [ref=f1e131]: D
                      - generic [ref=f1e132]: Đáp án đúng
                    - generic [ref=f1e133]:
                      - generic [ref=f1e134]: Nội dung phương án
                      - textbox "Nội dung phương án" [ref=f1e135]
                    - generic [ref=f1e136]:
                      - generic [ref=f1e137]: Image URL (không bắt buộc)
                      - textbox "Image URL (không bắt buộc)" [ref=f1e138]
                    - button "Xóa phương án D" [ref=f1e139] [cursor=pointer]
                - button "Thêm phương án" [ref=f1e140] [cursor=pointer]
              - generic [ref=f1e141]:
                - heading "Giải thích" [level=2] [ref=f1e142]
                - paragraph [ref=f1e143]: Chưa hỗ trợ lưu giải thích trong phiên bản hiện tại.
            - generic [ref=f1e144]:
              - button "Xem trước" [ref=f1e145] [cursor=pointer]
              - button "Lưu câu hỏi" [ref=f1e146] [cursor=pointer]
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
> 7  | test('USER creates and submits a question, SUBJECT_ADMIN approves it',async({page,gateway})=>{await gateway.login('USER');await page.goto('/questions/new');await page.getByLabel(/question content/i).fill('E2E question content');await page.getByLabel('Subject').selectOption(ids.subject);await page.getByLabel('Chapter').selectOption(ids.chapter);for(const input of await page.getByLabel(/option content/i).all())await input.fill('Answer content');await page.getByRole('radio',{name:/^A/}).check();await page.locator('main button').last().click();await expect(page).toHaveURL(new RegExp(`/questions/${ids.question}`));await page.locator('main button').last().click();await page.getByRole('button').last().click();const adminPage=await page.context().newPage();await gateway.login('SUBJECT_ADMIN',adminPage);await adminPage.goto(`/review/${ids.question}`);await adminPage.locator('main button').last().click();await adminPage.getByRole('button').last().click();await expect(adminPage.getByRole('status')).toBeVisible()})
     |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           ^ Error: locator.click: Test timeout of 30000ms exceeded.
  8  | test('notification can be marked read through the UI',async({page,gateway})=>{await gateway.login('USER');await page.goto('/notifications');await expect(page.getByText('Question approved').first()).toBeVisible();await page.locator('main button').first().click();await expect(page.getByText(/0 chÆ°a Ä‘á»c/i)).toBeVisible()})
  9  | test('AI job UI uses deterministic processing fixture',async({page,gateway})=>{await gateway.login('USER');await page.goto(`/ai/jobs/${ids.job}`);await expect(page.getByText(/PROCESSING|COMPLETED|Äang xá»­ lÃ½/i)).toBeVisible();await page.reload();await expect(page.getByText(/COMPLETED|Generated question/i)).toBeVisible()})
  10 | test('exam matrix generate flow reaches generated exam UI',async({page,gateway})=>{await gateway.login('SUBJECT_ADMIN');await page.goto('/exams/generate?matrixId='+ids.matrix);await page.getByLabel(/tÃªn bá»™ Ä‘á»/i).fill('E2E Exam');await page.locator('main button').last().click();await expect(page).toHaveURL(new RegExp(`/exams/${ids.exam}`))})
  11 | 
```