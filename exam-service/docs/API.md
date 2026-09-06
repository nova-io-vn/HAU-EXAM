> **Vị trí đặt file:** `backend/exam-service/docs/API.md`

# Exam Service --- API

Implemented synchronously under `/api/v1`: create/update/get/list/validate
exam matrices; create/get templates; generate/list/get exams; and create a new
exam version. All endpoints require `SUBJECT_ADMIN` and enforce the JWT faculty.
Export is not implemented in this phase.

Nhóm API: - `/api/v1/exam-matrices` - validate matrix -
`/api/v1/exams/generate` - `/api/v1/exams/{id}` - versions - export

Generate phải báo rõ lỗi khi ngân hàng câu hỏi APPROVED không đủ theo
matrix.
