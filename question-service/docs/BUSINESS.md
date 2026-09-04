> **Vị trí đặt file:** `backend/question-service/docs/BUSINESS.md`

# Question Service --- Business

## Actors

-   USER tạo/chỉnh/submit.
-   SUBJECT_ADMIN review theo faculty.
-   SYSTEM_ADMIN không mặc định review chuyên môn.
-   AI Service cung cấp kết quả tạo sinh.

## Domain

-   Subject → Chapter → Topic → Question.
-   Question có Option, difficulty, type, source, status, optional
    image.
-   Option có thể có text và optional image.

## Workflow

`DRAFT → PENDING_REVIEW → APPROVED | NEED_REVISION | REJECTED`.
`NEED_REVISION` quay lại quá trình chỉnh sửa trước khi submit lại. Có
thể `ARCHIVED`.

## Rules

-   Chỉ câu phù hợp workflow mới được approve.
-   SUBJECT_ADMIN phải cùng faculty.
-   AI-generated không tự approve.
-   Review history phải truy vết được.
