# 5. Cơ sở dữ liệu

| Service | Database logic | Migration thực tế |
|---|---|---|
| Auth | `auth_db` | V1 baseline, V2 auth accounts, V3 refresh tokens, V4 security snapshot |
| User | `user_db` | V1 baseline, V2 user profiles và inbox |
| Question | `question_db` | V1 baseline, V2 question schema |
| Exam | `exam_db` | V1 baseline, V2 exam schema |
| AI | `ai_db` | V1 baseline, V2 AI schema, V3 generation context |
| Notification | `notification_db` | V1 baseline, V2 notification schema, V3 device tokens |

Eureka và Gateway không có database nghiệp vụ. PostgreSQL có thể dùng chung một server trong Docker, nhưng service chỉ truy cập database của mình. Không có foreign key cross-service.

Flyway chạy khi service khởi động. Migration đã chạy trên môi trường thật không được sửa; thay đổi phải tạo version mới.
