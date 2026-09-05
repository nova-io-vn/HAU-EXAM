> **Vị trí đặt file:** `backend/exam-service/docs/DATABASE.md`

# Exam Service --- Database

Flyway V1 creates `exam_matrices`, `exam_matrix_rules`, `exam_templates`,
`exams`, `exam_versions`, and `exam_question_references`. Question, subject,
chapter, topic and user IDs are logical references; no cross-service foreign
key exists.

Database: `exam_db`.

Entity dự kiến: - ExamMatrix - ExamMatrixRule/Distribution -
ExamTemplate - Exam - ExamVersion - ExamQuestionReference

Question ID chỉ là logical reference. Không FK sang Question DB.
