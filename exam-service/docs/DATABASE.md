> **Vị trí đặt file:** `backend/exam-service/docs/DATABASE.md`

# Exam Service --- Database

Flyway V2 creates `exam_matrices`, `exam_matrix_rules`, `exam_templates`,
`exams`, `exam_versions`, and `exam_question_references`. Question, subject,
chapter, topic and user IDs are logical references; no cross-service foreign
key exists.

Flyway V4 adds knowledge-item buckets, generation configuration and seed/audit
metadata, persisted question counts, and `exam_version_options` for stable
answer display order. Existing versions are backfilled with their creator and
question count; no Question Service foreign key is introduced.

Database: `exam_db`.

Owned entities: ExamMatrix, ExamMatrixRule/Distribution, ExamTemplate, Exam,
ExamVersion, ExamQuestionReference, and ExamVersionOption.

Question ID chỉ là logical reference. Không FK sang Question DB.
