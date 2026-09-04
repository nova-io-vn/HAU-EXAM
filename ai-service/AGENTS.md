> **Vị trí đặt file:** `backend/ai-service/AGENTS.md`

# ai-service --- AGENTS.md

## Nghiệp vụ

AI Service là trung tâm xử lý tài liệu và AI, vẫn dùng Java/Spring Boot.
Quản lý upload metadata, extraction/parsing, AI Job, tạo sinh câu
hỏi/đáp án/phương án nhiễu/giải thích, phân tích
difficulty/topic/coverage và chatbot. Tác vụ dài phải chạy async. Không
có AI Worker microservice riêng; consumer nằm bên trong AI Service.

## Service sở hữu

Document metadata, AI Job, processing state, provider adapter,
structured generation result/reference, chatbot processing context phù
hợp.

## Không thuộc service

Không sở hữu ngân hàng câu hỏi chính thức. Không để HTTP chờ tác vụ AI
dài. Không gửi file lớn qua RabbitMQ.

## Database / Infrastructure

PostgreSQL `ai_db` + file/object storage; Redis cho job progress/temp
state khi phù hợp.

## API chính

Upload document; create/get AI job; generate questions; analysis;
chatbot endpoints.

## RabbitMQ

Produces/consumes `ai.generation.requested`, `ai.generation.completed`,
`ai.generation.failed` theo thiết kế queue nội bộ/event. Result phải
được validate trước khi Question Service nhận.

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
mvn -pl ai-service -am clean test
```

Hoặc trong thư mục module:

``` bash
mvn test
```
