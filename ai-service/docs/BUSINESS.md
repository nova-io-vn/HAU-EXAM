> **Vị trí đặt file:** `backend/ai-service/docs/BUSINESS.md`

# AI Service --- Business

## Module

-   Document.
-   Extraction/Parsing.
-   AI Job.
-   Generation.
-   Analysis.
-   Chatbot.

## Use cases

-   Upload tài liệu.
-   Trích xuất nội dung.
-   Tạo AI job.
-   Sinh câu hỏi/đáp án/nhiễu/giải thích.
-   Phân tích difficulty/topic/coverage.
-   Chatbot theo tài liệu/context cho phép.
-   Theo dõi job.

## Rules

-   Task dài async.
-   HTTP tạo job trả 202.
-   Không AI Worker service riêng.
-   AI output phải validate schema.
-   Provider là adapter, không gọi SDK trực tiếp từ Controller.
