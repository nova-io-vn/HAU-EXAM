> **Vị trí đặt file:** `backend/question-service/docs/DATABASE.md`

# Question Service --- Database

Flyway migration `V1__init_question_schema.sql` owns tables `subjects`,
`chapters`, `topics`, `questions`, `question_options`,
`question_review_history`, and `processed_events`. `faculty_id`, `created_by`,
and `reviewer_id` are logical references without cross-service foreign keys.
Media is stored only as `image_url` and `storage_key`.

Database: `question_db` PostgreSQL.

Entity dự kiến: - Subject - Chapter - Topic - Question -
QuestionOption - QuestionReviewHistory - optional OutboxEvent

Ảnh/file: lưu URL/storage key, không lưu binary trực tiếp trong bảng
question.

Index cần cân nhắc theo filter: faculty, subject, status, difficulty,
creator, createdAt.
