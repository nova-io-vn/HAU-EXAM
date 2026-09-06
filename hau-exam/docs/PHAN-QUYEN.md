# Phân quyền UI Web

Ba role duy nhất: `SYSTEM_ADMIN`, `SUBJECT_ADMIN`, `USER`.

| Role | Khu vực chính |
|---|---|
| USER | Questions, AI, Notifications, Profile |
| SUBJECT_ADMIN | Review, Questions, Notifications, Profile |
| SYSTEM_ADMIN | Approvals, Users, Notifications, Profile |

RoleGuard chỉ điều hướng UX. Backend vẫn kiểm tra JWT, role và faculty scope. Frontend không filter toàn bộ dữ liệu khoa để giả lập security.
