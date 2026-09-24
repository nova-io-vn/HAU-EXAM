# Phân quyền UI Web

Ba role duy nhất: `SYSTEM_ADMIN`, `SUBJECT_ADMIN`, `USER`.

| Role | Khu vực chính |
|---|---|
| USER | Questions, AI, Notifications, Profile |
| SUBJECT_ADMIN | Review, Questions, Notifications, Profile |
| SYSTEM_ADMIN | Approvals, Users, Notifications, Profile |

RoleGuard chỉ điều hướng UX. Backend vẫn kiểm tra JWT, role và faculty scope. Frontend không filter toàn bộ dữ liệu khoa để giả lập security.
## Phân công quản trị chuyên môn

`SYSTEM_ADMIN` có thể mở Quản lý Khoa → Sửa Khoa và chọn giảng viên trong searchable dropdown. Việc lưu gọi User Service để cập nhật đồng thời `role=SUBJECT_ADMIN` và `facultyId` của Khoa. User/giảng viên không thể tự đổi role hoặc khoa.
