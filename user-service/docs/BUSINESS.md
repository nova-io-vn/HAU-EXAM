> **Vị trí đặt file:** `backend/user-service/docs/BUSINESS.md`

# User Service --- Business

## Actors

-   SYSTEM_ADMIN.
-   SUBJECT_ADMIN.
-   USER.

## Use cases

-   Tạo profile khi nhận registration event.
-   Xem/cập nhật profile.
-   SYSTEM_ADMIN xem danh sách user.
-   Approve/reject đăng ký.
-   Gán role.
-   Gán/chuyển faculty.
-   Khóa/mở user ở góc độ nghiệp vụ.
-   Quản lý SUBJECT_ADMIN theo khoa.

## Rules

-   Chỉ 3 role.
-   Không Permission entity.
-   Lưu `dateOfBirth`, không lưu `age`.
-   SYSTEM_ADMIN không tự động trở thành SUBJECT_ADMIN.
