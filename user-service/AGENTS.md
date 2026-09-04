> **Vị trí đặt file:** `backend/user-service/AGENTS.md`

# user-service --- AGENTS.md

## Nghiệp vụ

User Service sở hữu hồ sơ người dùng và quản trị người dùng.
SYSTEM_ADMIN duyệt đăng ký, gán role, gán khoa, khóa/mở tài khoản ở góc
độ nghiệp vụ. SUBJECT_ADMIN được gán theo khoa. USER có hồ sơ cá nhân.
Chỉ có 3 role: SYSTEM_ADMIN, SUBJECT_ADMIN, USER; không có permission
matrix.

## Service sở hữu

User profile, lecturerCode reference, fullName, dateOfBirth, phone,
email, address, avatar, facultyId, role, profile/account business
status.

## Không thuộc service

Không xác minh password, không phát JWT, không lưu password. Không quản
lý câu hỏi hay đề thi.

## Database / Infrastructure

PostgreSQL `user_db`; Redis cache profile/faculty chỉ khi có nhu cầu.

## API chính

Profile APIs; SYSTEM_ADMIN user management; registration approval;
role/faculty assignment; lock/unlock.

## RabbitMQ

Consumes `user.registration.requested` để tạo profile/idempotent
registration record. Produces `user.approved`, `user.rejected`,
`user.role.changed`, `user.status.changed`, `user.faculty.changed` khi
phù hợp.

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
mvn -pl user-service -am clean test
```

Hoặc trong thư mục module:

``` bash
mvn test
```
