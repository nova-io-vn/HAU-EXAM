# Notification Service — Event Contracts

Consumer nhận envelope version 1 và dùng `processed_events.event_id` để idempotent. Main queue có retry hữu hạn và DLQ.

Recipient được map theo từng domain contract, không giả định payload nào cũng có `userId`:

| Event | Recipient field | Channels |
|---|---|---|
| `PASSWORD_RESET_OTP_REQUESTED` | `email` | EMAIL |
| `USER_APPROVED`, `USER_REJECTED` | `recipientUserId` | IN_APP, WEBSOCKET; EMAIL nếu có `email` |
| `QUESTION_SUBMITTED`, `QUESTION_APPROVED`, `QUESTION_REJECTED`, `QUESTION_REVISION_REQUESTED` | `createdBy` | IN_APP, WEBSOCKET |
| `AI_GENERATION_COMPLETED`, `AI_GENERATION_FAILED` | `requestedBy` | IN_APP, WEBSOCKET |
| `EXAM_GENERATED` | `requestedBy` hoặc `createdBy` theo Exam producer | IN_APP, WEBSOCKET |

OTP không được log, lưu Notification DB, hoặc gửi qua WebSocket. Event thiếu recipient bắt buộc là lỗi contract và không được ACK như thành công.

Scheduled notification resolve audience ACTIVE từ User Service qua internal REST theo `targetRole`, `targetFaculty`, hoặc cả hai; Notification Service không truy cập `user_db`.
