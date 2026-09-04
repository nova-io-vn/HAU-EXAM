> **Vị trí đặt file:** `backend/question-service/docs/EVENTS.md`

# Question Service --- Events

Produces: - `question.submitted` - `question.approved` -
`question.rejected` - `question.revision.requested`

Consumes: - `ai.generation.completed` hoặc result contract tương đương.

AI result consumer phải chống duplicate. Payload lớn nên dùng job/result
reference thay vì nhét tài liệu/file vào message.
