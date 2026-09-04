> **Vị trí đặt file:** `backend/api-gateway/docs/BUSINESS.md`

# API Gateway --- Business/Infrastructure

Gateway không có domain nghiệp vụ độc lập. Nó là ingress: routing, JWT
boundary validation, CORS, Redis rate limiting, correlation ID, logging.
Không truy cập business DB và không quyết định faculty ownership.
