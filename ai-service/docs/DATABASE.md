> **Vị trí đặt file:** `backend/ai-service/docs/DATABASE.md`

# AI Service --- Database

Flyway V1 creates `documents`, `ai_jobs`, `ai_results`, and
`processed_events`. File bytes remain in storage; PostgreSQL contains metadata,
durable job state, validated result JSON, and the idempotency inbox.

Database: `ai_db`.

Entity dự kiến: - DocumentMetadata - AiJob - AiResult/ResultReference -
optional ChatSession/ChatMessage nếu cần persistence - optional
OutboxEvent

File lưu object/local storage. DB giữ storage key, MIME type, metadata.

Redis có thể giữ temporary job progress, nhưng DB là durable source cho
trạng thái business quan trọng.
