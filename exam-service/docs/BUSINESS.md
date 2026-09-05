> **Vị trí đặt file:** `backend/exam-service/docs/BUSINESS.md`

# Exam Service --- Business

Generation is synchronous for the current small selection workload. Every
candidate returned by Question Service is revalidated as APPROVED and in the
matrix faculty/catalog scope before only its logical ID is persisted. A new
generation creates version 1; regeneration appends an immutable numbered
version.

## Mục tiêu

Xây ma trận và bộ đề từ ngân hàng câu hỏi đã duyệt.

## Use cases

-   Tạo/sửa ma trận.
-   Chọn subject/phạm vi kiến thức.
-   Cấu hình số câu theo chapter/topic/difficulty.
-   Validate tổng số câu và phân bố.
-   Generate bộ đề.
-   Tạo version.
-   Xem coverage.
-   Export nếu được triển khai.

## Out of scope

Không làm bài thi, submission, scoring, countdown, anti-cheat.
