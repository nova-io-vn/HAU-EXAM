> **Vị trí đặt file:** `backend/ai-service/docs/EVENTS.md`

# AI Service --- Events

Queue/event: - `ai.generation.requested` - `ai.generation.completed` -
`ai.generation.failed`

AI consumer nằm trong cùng deployable `ai-service`.

Cần: - manual ACK; - retry có delay; - DLQ; - concurrency giới hạn theo
provider quota; - idempotency theo jobId/eventId.
