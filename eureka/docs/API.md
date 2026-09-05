> **Vị trí đặt file:** `backend/eureka-server/docs/API.md`

# Eureka Server --- Interface

Port chuẩn 8761. Business services đăng ký/fetch registry qua
`defaultZone`. Không expose business endpoint.

- Dashboard: `GET /`
- Client registry base URL: `/eureka/`
- Health: `GET /actuator/health`

Tên đăng ký dự kiến: `api-gateway`, `auth-service`, `user-service`,
`question-service`, `exam-service`, `ai-service`, `notification-service`.
