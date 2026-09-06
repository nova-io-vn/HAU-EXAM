# Browser E2E

> Tài liệu tiếng Việt và trạng thái kiểm thử cập nhật nằm tại [KIEM-THU.md](KIEM-THU.md).

HAU-EXAM uses Playwright for browser-level critical-flow coverage. Tests drive the real React UI and mock only the API Gateway boundary; they never call internal microservice ports or an external AI provider.

## Commands

```bash
npm install
npx playwright install chromium
npm run test:e2e
```

Optional:

```bash
E2E_BASE_URL=http://127.0.0.1:4173 npm run test:e2e
```

## Test accounts

The current suite uses deterministic fixture identities (`E2E_USER`, `E2E_ADMIN`, and `SUBJECT_ADMIN`) handled by the Gateway mock. They are not production credentials. A live-environment suite must provide `E2E_USER_CODE`, `E2E_ADMIN_CODE`, and corresponding secrets through CI secret storage; no credentials belong in source control.

## Coverage

- Login
- Register → pending approval
- Forgot password → OTP → reset screens
- SYSTEM_ADMIN approval
- USER question creation/submission
- SUBJECT_ADMIN review/approval
- Notification mark-all-read
- AI job processing fixture
- Exam matrix → generate flow
