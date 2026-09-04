> **Vị trí đặt file:** `backend/api-gateway/AGENTS.md`

# api-gateway --- AGENTS.md

## Nghiệp vụ

API Gateway là external ingress. Route `/api/v1/**` tới service,
validate JWT ở biên, CORS, rate limiting bằng Redis, correlation ID và
logging cơ bản. Gateway không được biết business ownership chi tiết.

## Service sở hữu

Route config, gateway security/filter, rate limit, correlation ID
propagation.

## Không thuộc service

Không DB nghiệp vụ, không Question/User business logic, không quyết định
SUBJECT_ADMIN có sở hữu khoa/câu hỏi cụ thể hay không.

## Database / Infrastructure

Không có business DB. Redis có thể dùng rate limit.

## API chính

Expose route tới các service; không tạo duplicate business endpoint.

## RabbitMQ

Không dùng RabbitMQ nếu không có nhu cầu hạ tầng rõ ràng.

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
mvn -pl api-gateway -am clean test
```

Hoặc trong thư mục module:

``` bash
mvn test
```
