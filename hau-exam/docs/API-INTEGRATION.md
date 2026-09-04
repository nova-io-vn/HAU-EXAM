> **Vị trí đặt file:** `frontend/docs/API-INTEGRATION.md`

# API Integration

## HTTP

Browser chỉ gọi Gateway base URL.

``` text
React -> API Gateway -> Backend Service
```

Dùng một API client chung để: - attach Authorization; - parse
ApiResponse; - xử lý 401/403; - normalize error; - tránh duplicate code.

## WebSocket

``` text
React -> Gateway -> Notification Service
```

Subscribe user-specific destination:

`/user/queue/notifications`

Sau reconnect phải gọi REST notification list/unread count để đồng bộ
durable state.

## Async AI

Tạo AI job nhận `202 + jobId`. UI theo dõi trạng thái qua API/WebSocket
phù hợp; không giữ request HTTP mở.
