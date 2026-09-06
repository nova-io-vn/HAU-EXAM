# Question Service --- Events

Question Service publishes standard envelopes to `question.exchange` with
routing keys `question.submitted`, `question.approved`, `question.rejected`,
and `question.revision.requested`.

Question notification payload preserves `createdBy` as the semantic recipient
(question author). Notification Service maps this field explicitly; producers
do not rename it to a generic `userId`.

It consumes `ai.generation.completed` from `ai.exchange` using the same version
1 payload contract as AI Service:

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

The message contains context and a result reference only. Question Service
does not access `ai_db`; its `AiResultClient` adapter retrieves the structured
result through AI Service REST. The result is validated before import. Each
generated item becomes `QuestionSource.AI` with status `DRAFT`; it is never
auto-approved. Faculty/subject/chapter context comes from the event and an
item topic may override the event topic when present.

Imports are idempotent by `eventId` and `jobId:sourceId`. A redelivery therefore
does not create duplicate questions. Missing context, invalid structured
results, or temporary AI REST failures are processing failures: the consumer
uses the existing finite retry strategy and DLQ
`question.ai-generation.dlq`; it does not record the event as successfully
processed or create partial questions.
