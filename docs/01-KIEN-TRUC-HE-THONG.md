# 2. Kiến trúc hệ thống

```text
React Web / React Native Mobile
            |
            v
       API Gateway :8080
            |
   Eureka discovery + REST
            |
 Auth  User  Question  Exam  AI  Notification
            ^       ^       ^
            +-------RabbitMQ events-------+
                         |
              WebSocket / Email / AI provider
```

| Cơ chế | Mục đích |
|---|---|
| REST | Query/command cần phản hồi trực tiếp và service-to-service API |
| RabbitMQ | Event, email, AI job và xử lý nền |
| Eureka | Discovery cho các service dùng `lb://SERVICE-NAME` |
| WebSocket/STOMP | Notification realtime tới client |
| Redis | OTP, rate limit và dữ liệu tạm theo từng service |

Client chỉ đi qua Gateway; không dùng port nội bộ. Mỗi service sở hữu database logic riêng, không truy cập database của service khác.

Business service tổ chức theo Clean Architecture: `domain`, `application`, `infrastructure`, `presentation`. Gateway và Eureka là service hạ tầng.
