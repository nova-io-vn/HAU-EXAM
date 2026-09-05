> **Vị trí đặt file:** `backend/api-gateway/docs/BUSINESS.md`

# API Gateway --- Business/Infrastructure

Gateway không có domain nghiệp vụ độc lập. Nó là ingress: routing, JWT
boundary validation, CORS, Redis rate limiting, correlation ID, logging.
Không truy cập business DB và không quyết định faculty ownership.

Implementation dùng Spring Cloud Gateway WebFlux và Eureka load balancing.
Login/OTP/reset dùng Redis rate-limit theo địa chỉ kết nối với policy chặt
hơn notification read API. CORS chỉ cho origin cấu hình bởi
`FRONTEND_ALLOWED_ORIGIN`; không dùng wildcard khi bật credentials.

Identity header do client gửi bị xóa trước khi request được forward. Gateway
chỉ dựng lại các header đó từ JWT hợp lệ. Correlation ID chỉ nhận ký tự an
toàn và tối đa 64 ký tự; giá trị thiếu hoặc bất thường được thay bằng UUID.
