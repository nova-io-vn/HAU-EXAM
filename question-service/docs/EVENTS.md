> **Vị trí đặt file:** `backend/question-service/docs/EVENTS.md`

# Question Service --- Events

## Implemented contract

Publishes standard envelopes to `question.exchange` with routing keys
`question.submitted`, `question.approved`, `question.rejected`, and
`question.revision.requested`. The payload carries `questionId`, `facultyId`,
`createdBy`, and `status`.

Consumes `ai.generation.completed` from `ai.exchange`. The payload carries
`jobId`, `requestedBy`, catalog/faculty references, and generated items with a
stable `sourceId`. Imports are idempotent by `eventId` and `jobId:sourceId`;
every AI question starts at `DRAFT`. Delivery uses manual ACK, delayed finite
retry (three attempts by default), then `question.ai-generation.dlq`.

Produces: - `question.submitted` - `question.approved` -
`question.rejected` - `question.revision.requested`

Consumes: - `ai.generation.completed` hoặc result contract tương đương.

AI result consumer phải chống duplicate. Payload lớn nên dùng job/result
reference thay vì nhét tài liệu/file vào message.
