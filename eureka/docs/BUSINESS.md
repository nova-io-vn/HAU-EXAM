> **Vị trí đặt file:** `backend/eureka-server/docs/BUSINESS.md`

# Eureka Server --- Business/Infrastructure

Chỉ service discovery/registration. Không có domain nghiệp vụ, DB, Redis
hay RabbitMQ.

Server chạy standalone trên port 8761, không tự đăng ký và không fetch
registry từ Eureka khác. Dashboard/registry endpoint do Eureka Server cung
cấp; module không có domain layer hoặc business controller.
