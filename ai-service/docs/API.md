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
