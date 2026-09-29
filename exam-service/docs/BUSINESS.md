> **Vị trí đặt file:** `backend/exam-service/docs/BUSINESS.md`

# Exam Service --- Business

Generation is synchronous for the current small selection workload. Every
candidate returned by Question Service is revalidated as APPROVED and in the
matrix faculty/catalog scope before its logical ID is persisted. Generation
uses an inclusive version-code range and treats every
chapter/topic/knowledge-item/difficulty rule as a hard bucket. Selection
prefers the least-used candidate with deterministic seeded tie-breaking.
Matrix correctness always takes precedence over uniqueness and shuffling.

Question order and option-ID display order are persisted per version. Student
PDFs never expose answers; answer keys derive letters from the persisted option
order and the correct stable option ID. Re-export reads the saved version and
does not generate a new composition.

## Mục tiêu

Xây ma trận và bộ đề từ ngân hàng câu hỏi đã duyệt.

## Use cases

-   Tạo/sửa ma trận.
-   Chọn subject/phạm vi kiến thức.
-   Cấu hình số câu theo chapter/topic/knowledge item/difficulty.
-   Validate tổng số câu và phân bố.
-   Generate bộ đề.
-   Tạo nhiều mã đề, cân bằng việc dùng lại câu hỏi và cảnh báo khi không thể
    khác nhau hoàn toàn.
-   Xem coverage.
-   Export PDF sinh viên, đáp án và ZIP nhiều mã đề.

## Out of scope

Không làm bài thi, submission, scoring, countdown, anti-cheat.
