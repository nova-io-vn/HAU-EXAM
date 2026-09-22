# Tích hợp API Mobile

Mọi request đi theo `React Native -> API Gateway -> Backend`. `src/config/env.ts` lấy `EXPO_PUBLIC_API_BASE_URL`; không gọi `auth-service:8081`, `question-service:8083` hoặc port nội bộ.

Shared client ở `src/services/api/client.ts` xử lý access token, refresh single-flight, ApiResponse và error normalization. Auth API map register/login/refresh/logout/forgot/verify/reset theo Auth Service contract.

Question, AI, Exam, Profile, Notification và Admin API nằm trong feature `api/` tương ứng. AI tạo job trả 202/jobId; mobile theo dõi lifecycle thay vì giữ HTTP request dài. Notification WebSocket chỉ realtime; REST là nguồn durable để sync lại sau reconnect.
