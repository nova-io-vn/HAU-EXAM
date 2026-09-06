# 1. Tổng quan hệ thống

HAU-EXAM là hệ thống hỗ trợ tạo sinh và quản lý ngân hàng câu hỏi trắc nghiệm cho Đại học Kiến trúc Hà Nội.

## Mục tiêu hiện tại

- Quản lý hồ sơ, tài khoản và vai trò người dùng.
- Tạo, chỉnh sửa, gửi duyệt và duyệt câu hỏi.
- Upload tài liệu và tạo câu hỏi bằng AI theo job bất đồng bộ.
- Quản lý exam matrix, template, exam và version.
- Lưu thông báo trong ứng dụng, gửi realtime qua WebSocket và email theo event.
- Áp dụng JWT, RBAC, faculty scope, validation, rate limit và secure logging.

## Ngoài phạm vi

Đây không phải nền tảng thi trực tuyến. Source hiện không có attempt, countdown, submit answer, grading hoặc anti-cheat.

## Thành phần

Backend gồm Eureka, API Gateway, Auth, User, Question, Exam, AI và Notification. Client gồm React Web và React Native/Expo Mobile. Hạ tầng dùng PostgreSQL, RabbitMQ và Redis.
