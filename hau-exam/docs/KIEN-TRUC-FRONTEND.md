# Kiến trúc Frontend Web

```text
App.jsx
  -> AppProviders / AppRouter
  -> ProtectedRoute + RoleGuard
  -> AppLayout / feature pages
  -> shared API client -> API Gateway
```

Feature được tách trong `src/features`: auth, users, questions, AI, exams và notifications. Shared UI nằm trong `src/components`; session nằm trong `src/stores/authStore.js`; environment tập trung tại `src/config/env.js`.

API client xử lý ApiResponse, error registry, 401/403, refresh single-flight và correlation. WebSocket notification dùng Gateway `/ws`; không xem client role guard là security boundary.
