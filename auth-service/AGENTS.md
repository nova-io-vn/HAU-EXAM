> **Vị trí đặt file:** `backend/auth-service/AGENTS.md`

# auth-service --- AGENTS.md

## Nghiệp vụ

Auth Service sở hữu credential và authentication. Nghiệp vụ chính: đăng
ký tài khoản giảng viên; đăng nhập bằng mã giảng viên và mật khẩu; chờ
SYSTEM_ADMIN duyệt trước khi tài khoản hoạt động; JWT access/refresh
token; logout; đổi mật khẩu; quên mật khẩu; tạo/xác minh OTP; reset mật
khẩu; khóa trạng thái credential khi nhận sự kiện nghiệp vụ phù hợp.

## Service sở hữu

Credential/account security record, password hash, token/refresh-token
state, OTP security flow, authentication status.

## Không thuộc service

Không sở hữu fullName, địa chỉ, số điện thoại, hồ sơ khoa chi tiết.
Không gửi SMTP trực tiếp. Không tự duyệt nghiệp vụ chuyên môn.

## Database / Infrastructure

PostgreSQL `auth_db`; Redis cho OTP/login attempt/token revocation nếu
được bật.

## API chính

`POST /api/v1/auth/register`, `/login`, `/refresh`, `/logout`,
`/forgot-password`, `/verify-otp`, `/reset-password`,
`/change-password`.

## RabbitMQ

Produces: `user.registration.requested`, `password.reset.otp.requested`
và security events cần thiết. Consumes: `user.approved`,
`user.rejected`, `user.status.changed` nếu workflow yêu cầu.

## Clean Architecture

Business service phải giữ:

``` text
domain/
application/
infrastructure/
presentation/
```

Với Gateway/Eureka, chỉ tạo layer/package thật sự cần; không giả lập
Domain nếu không có nghiệp vụ domain.

## Quy tắc bắt buộc

-   Java 21, Maven, Spring Boot 4.1.1, `application.yml`.
-   Không truy cập DB service khác.
-   Không expose JPA Entity.
-   Không hard-code secret.
-   Không log credential/token/OTP.
-   Dùng constructor injection.
-   Thay đổi contract phải cập nhật `docs/`.
-   Nếu nghiệp vụ chưa rõ, không tự bịa rule mới.

## Build/Test

Từ root backend:

``` bash
mvn -pl auth-service -am clean test
```

Hoặc trong thư mục module:

``` bash
mvn test
```
