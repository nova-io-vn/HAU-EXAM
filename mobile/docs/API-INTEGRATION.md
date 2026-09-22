> **Vị trí đặt file:** `mobile/docs/API-INTEGRATION.md`

# Mobile API Integration

Tất cả HTTP request chỉ tới Gateway base URL.

API client chung cần:
- Authorization injection
- timeout
- ApiResponse parsing
- normalized errors
- refresh token flow nếu backend support
- chống refresh loop

Nếu access token hết hạn, chỉ cho một refresh request chạy tại một thời điểm.

AI async:

```text
POST -> 202 + jobId -> poll/realtime -> COMPLETED/FAILED
```

Upload file phải nằm trong service/API layer, không rải logic multipart trong screen.
