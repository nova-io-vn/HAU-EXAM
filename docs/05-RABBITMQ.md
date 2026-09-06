# 6. RabbitMQ

## Event chính

| Event | Producer | Consumer | Mục đích | Version |
|---|---|---|---|---|
| `user.registration.requested` | Auth | User | Tạo pending profile | 1 |
| `user.approved`/`rejected`/`status.changed`/`role.changed`/`faculty.changed` | User | Auth, Notification theo event | Đồng bộ snapshot và thông báo | 1 |
| `password.reset.otp.requested` | Auth | Notification | Gửi OTP email | 1 |
| `question.submitted`/`approved`/`rejected`/`revision.requested` | Question | Notification | Thông báo workflow | 1 |
| `ai.generation.requested` | AI | AI consumer | Xử lý job | 1 |
| `ai.generation.completed`/`failed` | AI | Question, Notification | Nhập kết quả/báo trạng thái | 1 |
| `exam.generated` | Exam | Notification nếu binding phù hợp | Báo exam tạo xong | 1 |

Envelope chuẩn gồm `eventId`, `eventType`, `correlationId`, `occurredAt`, `version`, `payload`.

Auth, User, Question, AI và Notification có queue/retry/DLQ theo cấu hình source. Consumer quan trọng dùng `eventId`, `jobId` hoặc source id để tránh dữ liệu trùng.
