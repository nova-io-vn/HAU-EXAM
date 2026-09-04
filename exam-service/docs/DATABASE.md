> **Vị trí đặt file:** `backend/exam-service/docs/DATABASE.md`

# Exam Service --- Database

Database: `exam_db`.

Entity dự kiến: - ExamMatrix - ExamMatrixRule/Distribution -
ExamTemplate - Exam - ExamVersion - ExamQuestionReference

Question ID chỉ là logical reference. Không FK sang Question DB.
