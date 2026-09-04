> **Vị trí đặt file:** `frontend/AGENTS.md`

# Frontend AGENTS.md

## Stack/role

Frontend là ReactJS web application. Mọi HTTP business request từ
browser phải đi qua API Gateway. Không gọi trực tiếp port nội bộ của
Auth/Question/User/... từ browser.

WebSocket cũng đi qua route Gateway tới Notification Service.

## Design

Bắt buộc đọc: - `docs/DESIGN_SYSTEM.md` - design system chi tiết của dự
án nếu được copy vào frontend.

Phong cách: modern Apple-inspired academic workspace; không clone Apple.

## Structure

``` text
src/
  app/
  components/ui/
  components/shared/
  features/auth/
  features/users/
  features/questions/
  features/exams/
  features/ai/
  features/notifications/
  services/api/
  services/websocket/
  hooks/
  stores/
  types/
  utils/
  styles/
```

## Role UI

-   SYSTEM_ADMIN: users, approvals, role/faculty/system management.
-   SUBJECT_ADMIN: faculty-scoped review/question/exam workflows.
-   USER: own questions, upload, AI generation, chatbot, notifications.

Frontend role visibility chỉ là UX. Backend vẫn phải authorize.

## API rules

-   Không tự bịa field.
-   Type phải bám API contract.
-   Chuẩn hóa API client.
-   Handle loading/error/empty state.
-   401/403 xử lý khác nhau.
-   Correlation ID từ response/error nên hiển thị/log ở mức hỗ trợ kỹ
    thuật khi phù hợp.

## Components

Reuse component trước khi tạo mới. Không tạo nhiều component gần giống
nhau chỉ khác vài class.

## Security

Không lưu secret trong frontend. Không đưa refresh/access token vào log.
Không tin client-side role là security boundary.
