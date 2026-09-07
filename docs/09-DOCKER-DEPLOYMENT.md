# 10. Docker và triển khai

`docker-compose.yml` định nghĩa PostgreSQL, Redis, RabbitMQ, Eureka, Gateway và sáu business service. Client Web/Mobile không nằm trong compose backend mặc định.

Trong container, service dùng hostname Compose như `postgres`, `redis`, `rabbitmq`, `eureka`, `auth-service`, `question-service`, không dùng `localhost`. Profile được chọn bằng `SPRING_PROFILES_ACTIVE=docker` từ Compose.

```powershell
docker compose --env-file .env.example config
docker compose up -d --build
docker compose ps
docker compose logs -f
docker compose down
```

`.env` không commit; chỉ commit `.env.example`. Volume PostgreSQL/RabbitMQ giữ dữ liệu phát triển. Xóa volume để reset là thao tác mất dữ liệu và chỉ dùng sau khi xác nhận.

Compose config đã được xác minh trong môi trường phát triển; runtime phụ thuộc Docker daemon.
# Bootstrap SYSTEM_ADMIN trong Docker

Có thể bật bootstrap ở môi trường development bằng các biến
`BOOTSTRAP_ADMIN_ENABLED`, `BOOTSTRAP_ADMIN_LECTURER_CODE`,
`BOOTSTRAP_ADMIN_EMAIL`, `BOOTSTRAP_ADMIN_FULL_NAME`,
`BOOTSTRAP_ADMIN_PASSWORD` và `BOOTSTRAP_ADMIN_FACULTY_ID`. Compose chỉ truyền
giá trị từ `.env`; không chứa password literal và không copy `.env` vào image.
Production phải thay placeholder bằng secret runtime, không dùng mật khẩu mặc
định.
