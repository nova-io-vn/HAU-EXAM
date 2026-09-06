# AI Service --- Events

`ai.exchange` carries standard envelopes:

```json
{
  "eventId": "uuid",
  "eventType": "AI_GENERATION_COMPLETED",
  "correlationId": "uuid",
  "occurredAt": "ISO-8601",
  "version": 1,
  "payload": {}
}
```

AI Service consumes `ai.generation.requested` internally and publishes
`ai.generation.completed` or `ai.generation.failed`. The completed event uses
a reference-based contract; generated question JSON is kept in `ai_db` and is
never embedded in RabbitMQ.

Notification Service maps `requestedBy` as the recipient for AI completed and
failed events. The field remains part of the AI domain contract and is not
renamed to a generic `userId`.

`ai.generation.completed` payload, version 1:

```json
{
  "jobId": "uuid",
  "requestedBy": "uuid",
  "facultyId": "CNTT",
  "subjectId": "uuid",
  "chapterId": "uuid",
  "topicId": "uuid-or-null",
  "resultReference": "db:ai-results:<jobId>"
}
```

`chapterId` and `topicId` may be null only when the generation request does not
specify them. Question import still requires the context needed by its domain
model (`facultyId`, `subjectId`, and `chapterId`). `resultReference` is stable
and is resolved through the authenticated internal AI result REST endpoint; it
is not a public download URL.

The event carries no document binary and no large generated result. Delivery
uses manual ACK, finite retry, DLQ `ai.generation.dlq`, and inbox idempotency
by event id/job id. Browser job tracking remains owner-scoped HTTP polling.
