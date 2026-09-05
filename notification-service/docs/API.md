> **Vị trí đặt file:** `backend/notification-service/docs/API.md`

# Notification Service --- API

REST: - `GET /api/v1/notifications` -
`GET /api/v1/notifications/unread-count` - mark read - mark all read -
scheduled notification management cho role phù hợp

WebSocket: - handshake `/ws` - user destination
`/user/queue/notifications`

JWT/Principal xác định user; client không tự chọn userId để subscribe dữ
liệu người khác.

## Contract implemented

- `GET /api/v1/notifications?page=0&size=20`
- `GET /api/v1/notifications/unread-count`
- `POST /api/v1/notifications/{id}/read`
- `POST /api/v1/notifications/read-all`
- `POST /api/v1/scheduled-notifications` (`SYSTEM_ADMIN`)

All user notification APIs derive user id from JWT `sub`.
