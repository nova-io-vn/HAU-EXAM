> **Vị trí đặt file:** `backend/auth-service/docs/BUSINESS.md`

# Auth Service --- Business

## Actors

-   USER/giảng viên đăng ký và đăng nhập.
-   SYSTEM_ADMIN gián tiếp quyết định account có được kích hoạt qua
    approval workflow.
-   Notification Service gửi OTP email.

## Use cases

1.  Register bằng lecturerCode, password và dữ liệu đăng ký tối thiểu
    cần thiết.
2.  Credential mới ở `PENDING_APPROVAL`.
3.  Login bằng lecturerCode/password; chỉ cho phép trạng thái hợp lệ.
4.  Issue access/refresh token.
5.  Refresh/logout.
6.  Change password.
7.  Forgot password → tạo OTP → Redis TTL → event gửi Notification.
8.  Verify OTP/reset password.
9.  Nhận approval/rejection/status event từ User Service.

## Rules

-   lecturerCode phải unique ở phạm vi identity.
-   Password không lưu plain text.
-   OTP hết hạn, giới hạn attempt và invalidate sau khi dùng thành công.
-   Không tự gửi SMTP.
