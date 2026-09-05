> **Vị trí đặt file:** `backend/ai-service/docs/EVENTS.md`

# AI Service --- Events

`ai.exchange` carries standard envelopes. AI Service publishes and internally
consumes `ai.generation.requested`, then publishes either
`ai.generation.completed` with a result reference or `ai.generation.failed`
with a safe error code. The durable queue uses manual ACK, delayed finite retry,
DLQ `ai.generation.dlq`, inbox idempotency by event id, and configurable limited
consumer concurrency (default 1). Large source/result content is never placed
on the message.

Queue/event: - `ai.generation.requested` - `ai.generation.completed` -
`ai.generation.failed`

AI consumer nằm trong cùng deployable `ai-service`.

Cần: - manual ACK; - retry có delay; - DLQ; - concurrency giới hạn theo
provider quota; - idempotency theo jobId/eventId.
