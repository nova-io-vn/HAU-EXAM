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

## Device token API

- `POST /api/v1/notifications/devices` registers or reactivates a device token.
- `DELETE /api/v1/notifications/devices` deactivates a device token.

Request body:

```json
{
  "token": "ExponentPushToken[...]",
  "platform": "ANDROID",
  "deviceIdentifier": "optional-device-id"
}
```

The authenticated JWT principal determines the owner; clients cannot submit a
user id. Invalid provider tokens are deactivated when the push provider reports
them as revoked.

Expo delivery is disabled by default. Enable it with `EXPO_PUSH_ENABLED=true`
and configure `EXPO_PUSH_URL` when the deployment is ready for provider delivery.

## Email settings API

All endpoints require `SYSTEM_ADMIN`:

- `GET /api/v1/admin/email-settings` returns the persisted SMTP settings and
  `passwordConfigured`; it never returns the SMTP password.
- `PUT /api/v1/admin/email-settings` persists runtime SMTP settings. A missing,
  blank, or UI placeholder password retains the existing encrypted password.
- `POST /api/v1/admin/email-settings/test` sends one test message with the
  current persisted settings.

Gmail supports `STARTTLS` on port `587` and implicit `SSL_TLS` on port `465`.
Invalid combinations return `SMTP_CONFIGURATION_INVALID` before a connection is
attempted. Delivery errors use `SMTP_CONNECTION_FAILED`,
`SMTP_AUTHENTICATION_FAILED`, `SMTP_TLS_FAILED`,
`SMTP_CREDENTIAL_DECRYPTION_FAILED`, `EMAIL_DELIVERY_DISABLED`, or
`EMAIL_SEND_FAILED`.

## Contact API

- `POST /api/v1/public/contact`
- `GET /api/v1/admin/contact` and `GET /api/v1/admin/contact/{id}`
- `PATCH /api/v1/admin/contact/{id}/status`
- `POST /api/v1/admin/contact/{id}/reply`

Contact replies use the same central `EmailSender` implementation as event email
delivery and therefore use the persisted runtime SMTP settings.
