# User Service

## 1. Mục đích

Quản lý user profile, role, faculty, status, approval và account management. `Auth credential` và `User profile` là hai bounded responsibility khác nhau.

## 2. Domain/database

Package `com.userservice`; `UserProfile`, `Role`, `UserStatus`. Database `user_db`, migrations tạo profile và processed event inbox. Profile có lecturerCode, fullName, dateOfBirth, phone, email, address, avatar, facultyId, role, status; tuổi là giá trị tính từ dateOfBirth ở response.

## 3. API chính

`GET/PUT /api/v1/users/me`; `GET /api/v1/users/me/chat-contacts` trả projection tên/avatar phục vụ giao diện chat (SYSTEM_ADMIN nhận USER/SUBJECT_ADMIN; người dùng nhận SYSTEM_ADMIN). SYSTEM_ADMIN dùng `GET /api/v1/users`, `GET /api/v1/users/{id}`, approve/reject/lock/unlock và assign role/faculty. `GET /api/v1/admin/analytics/traffic` tổng hợp visitor/pageview hôm nay, 7 ngày và tháng hiện tại từ Vercel Web Analytics; `GET /api/v1/admin/platform/cloudinary` chỉ trả trạng thái cấu hình, không trả secret. Internal audience API là `GET /api/v1/internal/users/audience`, xác thực bằng internal token.

## 4. Event

Consume `user.registration.requested`; publish các thay đổi `user.approved`, `user.rejected`, `user.status.changed`, `user.role.changed`, `user.faculty.changed`. Event có `userId`, profile/security fields cần thiết, version và correlationId; inbox giúp idempotency.

## 5. Security/config/test

Role guard SYSTEM_ADMIN nằm ở controller/application; Auth snapshot nhận thay đổi. Faculty scope là rule service, không phải client security. Vercel adapter đọc `VERCEL_ANALYTICS_TOKEN`, `VERCEL_ANALYTICS_PROJECT_ID` và tùy chọn `VERCEL_ANALYTICS_TEAM_ID`; token không được trả về frontend hoặc ghi log. Port `8082`; DB/Rabbit/Eureka theo dev/docker/prod profile. `mvn -f user-service/pom.xml test` và root verify PASS.
