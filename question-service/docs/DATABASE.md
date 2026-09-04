> **Vị trí đặt file:** `backend/question-service/docs/DATABASE.md`

# Question Service --- Database

Database: `question_db` PostgreSQL.

Entity dự kiến: - Subject - Chapter - Topic - Question -
QuestionOption - QuestionReviewHistory - optional OutboxEvent

Ảnh/file: lưu URL/storage key, không lưu binary trực tiếp trong bảng
question.

Index cần cân nhắc theo filter: faculty, subject, status, difficulty,
creator, createdAt.
