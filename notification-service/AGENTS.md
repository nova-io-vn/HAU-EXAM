> **Vị trí đặt file:** `backend/notification-service/AGENTS.md`

# notification-service --- AGENTS.md

## Nghiệp vụ

Notification Service là trung tâm thông báo. Nó nhận domain/security
events qua RabbitMQ rồi quyết định delivery channel. IN_APP được lưu DB;
WEBSOCKET/STOMP dùng realtime tới browser; EMAIL dùng SMTP cho thông báo
quan trọng như OTP; scheduled/system notification được lưu và phát theo
lịch bằng Spring Scheduler.

## Service sở hữu

Notification, unread/read state, delivery metadata,
ScheduledNotification, WebSocket session/delivery logic, SMTP adapter.

## Không thuộc service

RabbitMQ không phải email transport cuối; WebSocket không thay RabbitMQ.
Service khác không tự gửi email nếu notification flow đã thuộc service
này.

## Database / Infrastructure

PostgreSQL `notification_db`; Redis không bắt buộc bản đầu.

## API chính

List notifications, unread count, mark read, mark all read, scheduled
notification management phù hợp role; WebSocket `/ws` và user
destination.

## RabbitMQ

Consumes question/user/auth/AI/exam events cần thông báo. Có thể produce
delivery-failed/audit event khi thực sự cần.

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
mvn -pl notification-service -am clean test
```

Hoặc trong thư mục module:

``` bash
mvn test
```
