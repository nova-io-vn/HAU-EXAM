> **Vị trí đặt file:** `backend/user-service/docs/DATABASE.md`

# User Service --- Database

Database: `user_db`.

Entity dự kiến: - `UserProfile` - `Faculty` nếu User Service là master
faculty catalog - optional `OutboxEvent`

UserProfile chính: `id`, `lecturerCode`, `fullName`, `dateOfBirth`,
`phone`, `email`, `address`, `avatar`, `facultyId`, `role`, `status`,
timestamps.

Không lưu password.
