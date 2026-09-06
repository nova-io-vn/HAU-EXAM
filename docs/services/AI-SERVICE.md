# AI Service

## 1. Mục đích

Upload document, lưu AI job, gọi provider, tạo structured question result và chatbot. Không có deployable AI Worker riêng.

## 2. Domain/database

Package `com.aiservice`; database `ai_db`; migrations V1-V3. Job có `PENDING`, `PROCESSING`, `COMPLETED`, `FAILED`, requestedBy, document/context và resultReference.

## 3. API chính

`POST/GET /api/v1/documents`, `/api/v1/documents/{id}`; `POST /api/v1/ai/generate/questions`, `/api/v1/ai/analyze`, `/api/v1/chat`; `GET /api/v1/ai/jobs/{id}`. Internal result endpoint nằm trong `AiWorkspaceController` và yêu cầu internal token. Chi tiết request/response phải đối chiếu controller tương ứng.

## 4. Async/event

Request generation trả job theo pattern 202; consumer xử lý `ai.generation.requested` và publish `ai.generation.completed`/`failed`. Completion payload gồm jobId, requestedBy, facultyId, subjectId, chapterId, topicId, resultReference. Retry phân loại malformed/non-retryable/retryable và có DLQ.

## 5. Security/config/test

JWT bảo vệ API người dùng. Internal result endpoint yêu cầu `X-Internal-Service-Token`; token bắt buộc trong Docker/prod. Provider key là `AI_API_KEY`, storage mặc định local profile. Port `8085`; root verify và internal-token/retry tests PASS. Live provider chưa test.
