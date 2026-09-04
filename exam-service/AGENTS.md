> **Vị trí đặt file:** `backend/exam-service/AGENTS.md`

# exam-service --- AGENTS.md

## Nghiệp vụ

Exam Service quản lý ma trận đề và tạo bộ đề từ câu hỏi đã APPROVED.
Nghiệp vụ gồm ExamMatrix, ExamTemplate, ExamVersion, phân bố câu theo
chương/chủ đề/độ khó, validate tổng số câu, lựa chọn/random câu phù hợp,
kiểm tra coverage và export đề nếu triển khai.

## Service sở hữu

Exam matrix, template, version, distribution rules, generated exam
composition metadata.

## Không thuộc service

Không có student attempt, answer submission, scoring, countdown,
anti-cheat hoặc giám sát thi. Không query Question DB trực tiếp.

## Database / Infrastructure

PostgreSQL `exam_db`; Redis chỉ cho matrix/generated temporary data khi
có lợi.

## API chính

CRUD matrix/template; validate matrix; generate exam; view version;
export nếu feature được triển khai.

## RabbitMQ

Có thể produce `exam.generated`, `exam.updated` hoặc
notification-related event khi nghiệp vụ cần. Không tạo event chỉ để
trang trí kiến trúc.

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
mvn -pl exam-service -am clean test
```

Hoặc trong thư mục module:

``` bash
mvn test
```
