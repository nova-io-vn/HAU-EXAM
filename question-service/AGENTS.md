> **Vị trí đặt file:** `backend/question-service/AGENTS.md`

# question-service --- AGENTS.md

## Nghiệp vụ

Question Service là core ngân hàng câu hỏi. Quản lý Subject, Chapter,
Topic, Question, QuestionOption, ảnh/reference file, độ khó, loại câu
hỏi, nguồn manual/AI và lịch sử review. USER tạo/chỉnh câu hỏi và
submit. SUBJECT_ADMIN review trong đúng khoa: approve, reject, request
revision. AI-generated question không tự động trở thành nội dung được
duyệt.

## Service sở hữu

Subject/Chapter/Topic phục vụ ngân hàng câu hỏi, Question, Option,
ReviewHistory, workflow status, question metadata.

## Không thuộc service

Không tổ chức thi, không chấm điểm. Không truy cập User DB. Không lưu
binary ảnh trực tiếp trong PostgreSQL.

## Database / Infrastructure

PostgreSQL `question_db`; Redis cho read-heavy
question/catalog/statistics nếu cần; file ở local/MinIO/S3-compatible
storage.

## API chính

CRUD/search/filter Question; Subject/Chapter/Topic APIs;
submit/review/approve/reject/request-revision/archive.

## RabbitMQ

Produces `question.submitted`, `question.approved`, `question.rejected`,
`question.revision.requested`. Consumes kết quả AI generation theo
contract đã chốt và phải idempotent.

## Clean Architecture

Business service phải giữ:

``` text
domain/
application/
infrastructure/
presentation/
```

Với Gateway/Eureka, chỉ tạo layer/package thật sự cần; không giả lập
Domain nếu không có nghiệp vụ domain.

## Quy tắc bắt buộc

-   Java 21, Maven, Spring Boot 4.1.1, `application.yml`.
-   Không truy cập DB service khác.
-   Không expose JPA Entity.
-   Không hard-code secret.
-   Không log credential/token/OTP.
-   Dùng constructor injection.
-   Thay đổi contract phải cập nhật `docs/`.
-   Nếu nghiệp vụ chưa rõ, không tự bịa rule mới.

## Build/Test

Từ root backend:

``` bash
mvn -pl question-service -am clean test
```

Hoặc trong thư mục module:

``` bash
mvn test
```
