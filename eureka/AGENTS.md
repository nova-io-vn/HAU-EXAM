> **Vị trí đặt file:** `backend/eureka-server/AGENTS.md`

# eureka-server --- AGENTS.md

## Nghiệp vụ

Eureka Server chỉ làm service registration/discovery và hiển thị trạng
thái instance. Đây là infrastructure service, không phải business
service.

## Service sở hữu

Service registry/discovery runtime.

## Không thuộc service

Không DB nghiệp vụ, không JWT business logic, không RabbitMQ, không
Redis, không domain use case.

## Database / Infrastructure

Không.

## API chính

Eureka infrastructure endpoints/dashboard; không expose business API.

## RabbitMQ

Không.

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
mvn -pl eureka-server -am clean test
```

Hoặc trong thư mục module:

``` bash
mvn test
```
