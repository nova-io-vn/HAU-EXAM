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
