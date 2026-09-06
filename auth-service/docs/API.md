> **Vị trí đặt file:** `backend/auth-service/docs/API.md`

# Auth Service --- API

`POST /api/v1/auth/register` nhận `lecturerCode`, `password`, `fullName`,
`dateOfBirth` (optional), `phone` (optional), `email`, `address` (optional),
`avatar` (optional), `facultyId` (optional). Thành công tạo credential và profile
workflow ở trạng thái `PENDING_APPROVAL`; không auto-login và không trả password.

Public JWKS: `GET /.well-known/jwks.json`.

Các endpoint mục tiêu:

  --------------------------------------------------------------------------------
  Method                  Path                             Mục đích
  ----------------------- -------------------------------- -----------------------
  POST                    `/api/v1/auth/register`          Đăng ký, trạng thái
                                                           pending

  POST                    `/api/v1/auth/login`             Login
                                                           lecturerCode/password

  POST                    `/api/v1/auth/refresh`           Cấp access token mới

  POST                    `/api/v1/auth/logout`            Thu hồi phiên/refresh
                                                           token

  POST                    `/api/v1/auth/forgot-password`   Bắt đầu OTP

  POST                    `/api/v1/auth/verify-otp`        Xác minh OTP

  POST                    `/api/v1/auth/reset-password`    Đặt mật khẩu mới

  POST                    `/api/v1/auth/change-password`   Đổi mật khẩu khi đã
                                                           đăng nhập
  --------------------------------------------------------------------------------

Không khóa request schema chi tiết trước khi entity/use case được duyệt.
