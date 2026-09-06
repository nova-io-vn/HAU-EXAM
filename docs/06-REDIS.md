# 7. Redis

Redis không thay thế PostgreSQL làm source of truth.

| Service | Sử dụng đã thấy trong source |
|---|---|
| Auth | OTP reset password và dữ liệu tạm xác thực |
| Gateway | Redis rate limiter |
| AI | Kết nối Redis được cấu hình; job chính vẫn có persistence |
| Các service khác | Không ghi nhận cache nghiệp vụ chung nếu source không có adapter tương ứng |

OTP có TTL. Cache/rate limit nếu bật phải có TTL/eviction phù hợp. Không lưu password plaintext, token nhạy cảm hoặc OTP trong log.
