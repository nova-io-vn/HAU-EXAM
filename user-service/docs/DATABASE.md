> **Vị trí đặt file:** `backend/user-service/docs/DATABASE.md`

# User Service --- Database

Database: `user_db`.

Entity dự kiến: - `UserProfile` - `Faculty` nếu User Service là master
faculty catalog - optional `OutboxEvent`

UserProfile chính: `id`, `lecturerCode`, `fullName`, `dateOfBirth`,
`phone`, `email`, `address`, `avatar`, `facultyId`, `role`, `status`,
timestamps.

Không lưu password.

## Schema implemented

- `user_profiles`: profile aggregate, unique `lecturer_code` and `email`, optimistic `version`.
- `processed_events`: durable consumer inbox keyed by `event_id`.

`faculty_id` is currently a logical reference. A Faculty catalog/entity is not introduced until ownership of the faculty master catalog is explicitly decided.
