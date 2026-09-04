> **Vị trí đặt file:** `backend/ai-service/docs/DATABASE.md`

# AI Service --- Database

Database: `ai_db`.

Entity dự kiến: - DocumentMetadata - AiJob - AiResult/ResultReference -
optional ChatSession/ChatMessage nếu cần persistence - optional
OutboxEvent

File lưu object/local storage. DB giữ storage key, MIME type, metadata.

Redis có thể giữ temporary job progress, nhưng DB là durable source cho
trạng thái business quan trọng.
