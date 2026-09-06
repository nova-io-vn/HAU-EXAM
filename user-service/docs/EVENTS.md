# User Service — Events

Mọi message dùng envelope version 1: `eventId`, `eventType`, `correlationId`, `occurredAt`, `version`, `payload`.

## Consume registration

User Service consume `auth.exchange` / `user.registration.requested` qua queue `user.registration.requested.queue`.

Payload: `userId`, `lecturerCode`, `fullName`, `dateOfBirth`, `phone`, `email`, `address`, `avatar`, `facultyId`.
Không có password/password hash. Consumer tạo profile `PENDING_APPROVAL`, idempotent theo `eventId`, `userId` và identity; retry tối đa 3 lần rồi vào `user.registration.requested.dlq`.

## Publish security synchronization

User Service publish lên `user.exchange`:

- `user.approved`
- `user.rejected`
- `user.status.changed`
- `user.role.changed`
- `user.faculty.changed`

Payload chung: `userId`, `lecturerCode`, `role`, `facultyId`, `status`, `email`, `recipientUserId`.
`recipientUserId` là applicant/user nhận thông báo cho approval/rejection; `userId` vẫn là identity được Auth đồng bộ.
