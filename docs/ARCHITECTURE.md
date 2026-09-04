> **Vị trí đặt file:** `docs/ARCHITECTURE.md`

# Architecture

## Tổng quan

Hệ thống dùng Microservices + Clean Architecture + Event-Driven
Architecture.

``` text
ReactJS
   |
API Gateway
   |
   +--> Auth
   +--> User
   +--> Question
   +--> Exam
   +--> AI
   +--> Notification

Business services <--> Eureka Discovery
Business events ------> RabbitMQ ------> Consumers
Notification Service --> WebSocket ----> ReactJS
```

## Service ownership

Mỗi service sở hữu domain và database của mình. ID từ service khác chỉ
là logical reference; không tạo foreign key xuyên database.

## Giao tiếp

-   REST: cần phản hồi đồng bộ.
-   RabbitMQ: event, email request, AI processing, background work.
-   Redis: cache/temporary state, không phải source of truth.
-   WebSocket: realtime tới browser.
-   Gateway: external ingress, không chứa domain logic.

## Clean Architecture

``` text
presentation -> application -> domain
infrastructure -> implements ports
```

Domain không phụ thuộc Spring MVC, JPA, Redis, RabbitMQ, SMTP hoặc AI
SDK.

## Quy tắc thay đổi kiến trúc

Không tự ý thêm service, gộp service, thêm Keycloak, gRPC, MongoDB hoặc
một framework mới chỉ để tăng độ phức tạp. Nếu có vấn đề thiết kế, Agent
phải nêu vấn đề và đề xuất thay đổi tối thiểu trước.
