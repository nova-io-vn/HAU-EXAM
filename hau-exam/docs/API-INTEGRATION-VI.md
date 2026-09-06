# Tích hợp API Web

## Nguyên tắc

```text
React Web -> http://<gateway>/api/... -> Spring service
```

Base URL lấy từ `VITE_API_BASE_URL`; WebSocket lấy từ `VITE_WS_BASE_URL` nếu cấu hình riêng. Không hard-code microservice port.

## Client dùng chung

`src/services/api/client.js` là shared client. Feature API chỉ xây path/backend contract, không tạo client thứ hai. Response chuẩn là `{success, code, message, data}`; mã lỗi đi qua `src/services/api/errorMessages.js`.

Các nhóm endpoint chính: `/api/v1/auth/*`, `/api/v1/users/*`, `/api/v1/questions/*`, `/api/v1/exam-matrices/*`, `/api/v1/exams/*`, `/api/v1/ai/*`, `/api/v1/documents/*`, `/api/v1/notifications/*`.

401 xóa session và chuyển Login, tránh loop trên Auth pages. 403 giữ session và hiển thị Forbidden. Notification read dùng POST theo backend contract.
