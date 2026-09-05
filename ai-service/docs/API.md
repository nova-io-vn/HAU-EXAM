> **Vị trí đặt file:** `backend/ai-service/docs/API.md`

# AI Service --- API

Implemented endpoints: `POST /api/v1/documents`, `GET /api/v1/documents/{id}`,
`POST /api/v1/ai/generate/questions`, `POST /api/v1/ai/analyze`,
`POST /api/v1/chat`, and `GET /api/v1/ai/jobs/{id}`. Processing endpoints
return HTTP 202 with a durable job id. Document and job reads are owner scoped
from the authenticated JWT subject.

Nhóm API: - `/api/v1/documents` - `/api/v1/ai/jobs` -
`/api/v1/ai/generate/questions` - `/api/v1/ai/analyze` - `/api/v1/chat`

Job endpoint trả `jobId` và trạng thái. Không giả progress percentage
nếu backend không có dữ liệu thật.

## Workspace reads

All endpoints require JWT. Owner is derived from `sub`, never a client user ID.
Lists query by owner in persistence, without client-side security filtering.

| Method | Path | Response data |
|---|---|---|
| GET | `/api/v1/documents?page=0&size=20` | Page of DocumentView |
| GET | `/api/v1/ai/jobs?page=0&size=20` | Page of JobView |
| GET | `/api/v1/ai/jobs/{id}/result` | Persisted JSON result |

Page: `{items,page,size,totalElements,totalPages}`; zero-based page, size 1–100.
Sort: createdAt descending, then ID. Invalid pagination: 400. Missing or another
owner's resource: 404. Result before COMPLETED: 409. Standard API envelopes apply.

Upload is multipart `file`, nonempty UTF-8 text/plain. The service size limit
defaults to 10485760 bytes (`AI_DOCUMENT_MAX_SIZE_BYTES`). Multipart file/request
limits also apply (10MB); request overhead can cause rejection near the boundary.
HTTP 201 returns `{id,originalName,contentType,size,storageKey,checksum,createdAt}`.
There is no independent document processing status; extraction runs in AI jobs.

Commands:
- Generate: `{documentId,count,difficulty?,topicId?}`; count 1–100, topic UUID.
- Analyze: `{documentId,analysisType}`; analysisType is a nonblank request string.
- Chat: `{documentId?,message}`; nonblank message, max 4000 characters. Previous
  conversation turns are not supplied implicitly as context.

HTTP 202 JobView: `{jobId,type,status,resultReference,errorCode,errorMessage,
createdAt,startedAt,completedAt}`. Status: PENDING, PROCESSING, COMPLETED, FAILED.
Poll the owner-scoped job endpoint until terminal status. No percentage or
browser WebSocket progress event is defined.

Generation result: array of `{question,options:[{label,content}],correctAnswer,
explanation,difficulty,topicId?}`. Analysis/chat: JSON object; chat provider asks
for an `answer` field. References are optional provider output. `resultReference`
is an internal reference, not a public download URL. AI results are not APPROVED
questions and must follow the Question Service review workflow.
