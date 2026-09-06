# Auth Service — Events

Mọi message dùng envelope version 1: `eventId`, `eventType`, `correlationId`, `occurredAt`, `version`, `payload`.

## Events được publish

### `user.registration.requested`

`eventType`: `USER_REGISTRATION_REQUESTED`.

Payload: `userId`, `lecturerCode`, `fullName`, `dateOfBirth`, `phone`, `email`, `address`, `avatar`, `facultyId`.
Đây là dữ liệu đủ để User Service tạo profile `PENDING_APPROVAL`. Event không chứa `password` hoặc `passwordHash`.

### `password.reset.otp.requested`

`eventType`: `PASSWORD_RESET_OTP_REQUESTED`.

Payload: `recipientUserId`, `email`, `otp`, `expiresAt`. Notification Service dùng `email` làm recipient và chỉ gửi qua EMAIL. OTP plaintext chỉ tồn tại trong message để giao nhận, không được log hoặc lưu vào Notification DB.

## Events được consume từ `user.exchange`

- `user.approved`
- `user.rejected`
- `user.status.changed`
- `user.role.changed`
- `user.faculty.changed`

Payload chung: `userId`, `lecturerCode`, `role`, `facultyId`, `status`, `email`, `recipientUserId`.
Auth cập nhật security snapshot `status/role/facultyId` và security contact. Consumer chỉ nhận version 1, idempotent theo `eventId`, retry hữu hạn và chuyển DLQ khi hết retry.
