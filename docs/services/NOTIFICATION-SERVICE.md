# Notification Service

## 1. Mục đích

Trung tâm tiếp nhận domain event và delivery notification: lưu in-app, WebSocket/STOMP, email SMTP, scheduled notification và device token/push adapter.

## 2. Domain/database

Package `com.notificationservice`; database `notification_db`; migrations tạo notifications, scheduled notifications/processed events và device tokens. Notification có userId, type, content, reference, read state.

## 3. API chính

`GET /api/v1/notifications`, `/unread-count`, `POST /api/v1/notifications/{id}/read`, `POST /api/v1/notifications/read-all`; `POST/DELETE /api/v1/notifications/devices` cho device token; scheduled notification controller. WebSocket endpoint `/ws`, user destination `/user/queue/notifications`.

## 4. Event/recipient semantics

Consume event từ auth, user, question, AI và exam. `QUESTION_*` nhận recipient từ `createdBy`; `AI_*` từ `requestedBy`; registration/user approval từ `recipientUserId`; OTP dùng email delivery. Event version được kiểm tra và xử lý idempotent.

## 5. Scheduled/push/config

Scheduled audience resolve qua User Service REST, không truy cập user DB; empty audience/failure chuyển FAILED. Expo push adapter và device-token API có trong source, nhưng provider thật/OS permission chưa live test. SMTP, WebSocket origin, User Service URL và internal token lấy từ profile/env. Port `8086`; root verify PASS.
