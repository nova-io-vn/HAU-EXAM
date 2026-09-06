# API Gateway

## 1. Mục đích

Điểm vào duy nhất của Web/Mobile, source root `com.gateway`, application name `api-gateway`, port `8080`.

## 2. Trách nhiệm

Route qua Eureka, JWT validation, CORS, correlation ID, request logging cơ bản và Redis rate limiting. Không truy cập database hoặc quyết định faculty scope nghiệp vụ.

## 3. Route chính

| Path | Route |
|---|---|
| `/api/v1/auth/**` | `lb://AUTH-SERVICE` |
| `/api/v1/users/**`, `/api/v1/faculties/**` | `lb://USER-SERVICE` |
| `/api/v1/questions/**`, `/api/v1/subjects/**`, `/api/v1/chapters/**`, `/api/v1/topics/**` | `lb://QUESTION-SERVICE` |
| `/api/v1/exams/**`, `/api/v1/exam-matrices/**`, `/api/v1/exam-templates/**` | `lb://EXAM-SERVICE` |
| `/api/v1/ai/**`, `/api/v1/documents/**`, `/api/v1/chat/**` | `lb://AI-SERVICE` |
| `/api/v1/notifications/**`, `/api/v1/scheduled-notifications/**` | `lb://NOTIFICATION-SERVICE` |
| `/ws/**` | `lb:ws://NOTIFICATION-SERVICE` |

## 4. Security và cấu hình

Resource server đọc `JWT_JWK_SET_URI`. Dev dùng Eureka localhost; Docker dùng `eureka`. Redis rate limit cần `REDIS_HOST`, port và password. Production secret/env bắt buộc.

## 5. Test/chạy

`mvn -f gateway/pom.xml test`; root `mvn clean verify`. Gateway security/JWKS test đã có. Live route/discovery cần infrastructure thật.
