> **Vị trí đặt file:** `backend/api-gateway/docs/API.md`

# API Gateway --- Routes

| Route | Downstream |
|---|---|
| `/api/v1/auth/**` | `lb://auth-service` |
| `/api/v1/users/**`, `/api/v1/faculties/**` | `lb://user-service` |
| `/api/v1/questions/**`, `/api/v1/subjects/**`, `/api/v1/chapters/**`, `/api/v1/topics/**` | `lb://question-service` |
| `/api/v1/exams/**`, `/api/v1/exam-matrices/**`, `/api/v1/exam-templates/**` | `lb://exam-service` |
| `/api/v1/ai/**`, `/api/v1/documents/**`, `/api/v1/chat/**` | `lb://ai-service` |
| `/api/v1/notifications/**`, `/api/v1/scheduled-notifications/**` | `lb://notification-service` |
| `/ws/**` | `lb:ws://notification-service` |

Public: register, login, refresh, forgot-password, verify-otp,
reset-password, health và Swagger/OpenAPI. Các route còn lại yêu cầu JWT.

Gateway ghi đè `X-User-Id`, `X-Lecturer-Code`, `X-Role`,
`X-Faculty-Id` bằng claim từ JWT đã xác thực và propagate
`X-Correlation-Id`. Gateway không thực hiện authorization theo dữ liệu
nghiệp vụ/faculty ownership.
