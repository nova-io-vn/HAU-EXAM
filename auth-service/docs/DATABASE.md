> **Vị trí đặt file:** `backend/auth-service/docs/DATABASE.md`

# Auth Service --- Database

Database: `auth_db`.

Bảng dự kiến: - `credentials/accounts` - `refresh_tokens` -
`password_reset_requests` nếu cần audit durable - `outbox_events` nếu
dùng Transactional Outbox

Redis: - `auth:otp:<purpose>:<identity>` - login attempt/rate state -
optional JWT blacklist

Flyway quản lý schema. Không dùng `ddl-auto=update`.
