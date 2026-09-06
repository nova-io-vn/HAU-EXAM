# Auth Service

## 1. Mục đích và trách nhiệm

Quản lý credential, BCrypt password, registration, login, refresh token, logout, forgot-password/OTP và JWT. Auth không sở hữu full user profile, faculty metadata hay avatar.

## 2. Domain/database

Package root `com.authservice`. Domain chính: `AuthAccount`, `AccountStatus` gồm `PENDING_APPROVAL`, `ACTIVE`, `REJECTED`, `LOCKED`. Database `auth_db` có auth account, refresh token, security snapshot và processed auth events.

## 3. API chính

`POST /api/v1/auth/register`, `/login`, `/refresh`, `/logout`, `/forgot-password`, `/verify-otp`, `/reset-password`; `GET /.well-known/jwks.json`. Response dùng `ApiResponse`; password/hash không nằm trong response/event.

## 4. Event và integration

Publish `user.registration.requested` với envelope version 1 và payload profile đầy đủ, không password. Publish `password.reset.otp.requested` cho Notification. Consume `user.approved`, `user.rejected`, `user.status.changed`, `user.role.changed`, `user.faculty.changed` để cập nhật snapshot idempotent.

## 5. Security/Redis

RS256 ký bằng private key, JWKS chỉ public key. Role/faculty lấy từ account snapshot. OTP dùng Redis TTL; refresh token persistence/revocation dùng database. Public auth routes được mở theo từng path, không wildcard nhạy cảm.

## 6. Profiles/chạy/test

Port `8081`. Dev PostgreSQL/Redis/RabbitMQ/Eureka ở localhost; Docker dùng hostname. Prod cần key/env bắt buộc. `mvn -f auth-service/pom.xml test`; root verify đã PASS. Gửi mail trực tiếp từ Auth là NOT IMPLEMENTED; Notification đảm nhiệm delivery.
