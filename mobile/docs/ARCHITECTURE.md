> **Vị trí đặt file:** `mobile/docs/ARCHITECTURE.md`

# Mobile Architecture

> Bản tiếng Việt phục vụ bàn giao nằm tại [KIEN-TRUC-MOBILE.md](KIEN-TRUC-MOBILE.md).

```text
React Native
    |
API Gateway
    |
    +-- Auth
    +-- User
    +-- Question
    +-- Exam
    +-- AI
    +-- Notification
```

Mobile không truy cập PostgreSQL, Redis, RabbitMQ hoặc Eureka.

Layer:

```text
Screen
  -> Feature hook/use-case
  -> Repository/API abstraction
  -> HTTP/WebSocket/Secure Storage adapter
```

Tách server state, auth/session state và local UI state. Không đưa mọi thứ vào một global store.

Phân biệt lỗi network, validation, 401, 403, 404, business conflict và server error.
