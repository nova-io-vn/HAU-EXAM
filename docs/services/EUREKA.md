# Eureka Server

## 1. Mục đích

Service discovery cho các Spring service. Source root là `com.eureka` và application name là `eureka-server`.

## 2. Trách nhiệm / không thuộc trách nhiệm

Đăng ký, tra cứu và theo dõi instance. Không có business logic, authentication nghiệp vụ hoặc database nghiệp vụ.

## 3. Cấu trúc và package

Source chính là `EurekaServerApplication`. Đây là service hạ tầng, không áp dụng đầy đủ 4 layer business.

## 4. API, event, Redis

Không có API nghiệp vụ, RabbitMQ producer/consumer hay Redis use case.

## 5. Cấu hình

Port mặc định `8761`; dev dùng `http://localhost:8761/eureka/`; Docker dùng hostname `eureka`. Standalone không tự register/fetch registry.

## 6. Chạy và kiểm thử

Local: `mvn -f eureka/pom.xml spring-boot:run`. Docker: Compose profile `docker`. Health endpoint: `/actuator/health`. Test context nằm trong module; live discovery chưa được xác minh nếu Docker daemon không chạy.
