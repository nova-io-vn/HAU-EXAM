> **Vị trí đặt file:** `backend/notification-service/docs/EVENTS.md`

# Notification Service --- Events

Consumes các event cần thông báo: - password reset OTP request -
registration/approval status - question
submitted/approved/rejected/revision - AI completed/failed - exam
generated nếu cần

Consumer quyết định channel theo notification policy. Không assume mọi
event đều gửi email.

## Policy and topology implemented

- `PASSWORD_RESET_OTP_REQUESTED`: EMAIL only.
- User approval/rejection: IN_APP + WEBSOCKET; EMAIL when the event supplies an email address.
- Question, AI and Exam events: IN_APP + WEBSOCKET.
- Main queue: `notification.events.queue`.
- Retry queue: `notification.events.retry.queue`, configurable delay, maximum 3 attempts.
- DLQ: `notification.events.dlq`.

Consumers require the common event envelope and use `processed_events.event_id` for idempotency.
