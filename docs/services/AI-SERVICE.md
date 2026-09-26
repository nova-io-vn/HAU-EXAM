# AI Service

## 1. Mục đích

Upload document, lưu AI job, gọi provider, tạo structured question result và chatbot. Không có deployable AI Worker riêng.

## 2. Domain/database

Package `com.aiservice`; database `ai_db`; migrations V1-V3. Job có `PENDING`, `PROCESSING`, `COMPLETED`, `FAILED`, requestedBy, document/context và resultReference.

## 3. API chính

`POST/GET /api/v1/documents`, `/api/v1/documents/{id}`; `POST /api/v1/ai/generate/questions`, `/api/v1/ai/analyze`, `/api/v1/chat`; `GET /api/v1/ai/jobs/{id}`. Request tạo câu hỏi bắt buộc có `documentId`, `subjectId`, `chapterId`; `topicId` là tùy chọn. SYSTEM_ADMIN quản lý kho tri thức trợ lý qua `GET/POST /api/v1/admin/ai-policy/documents`, `PATCH /{id}?enabled=...`, `POST /{id}/reprocess` và `DELETE /{id}`; response tài liệu có `chunkCount` để giao diện theo dõi kết quả lập chỉ mục. Internal result endpoint nằm trong `AiWorkspaceController` và yêu cầu internal token. Chi tiết request/response phải đối chiếu controller tương ứng.

## 4. Async/event

Request generation trả job theo pattern 202; consumer xử lý `ai.generation.requested` và publish `ai.generation.completed`/`failed`. Completion payload gồm jobId, requestedBy, facultyId, subjectId, chapterId, topicId, resultReference. Retry phân loại malformed/non-retryable/retryable và có DLQ.

## 5. Security/config/test

JWT bảo vệ API người dùng. Internal result endpoint yêu cầu `X-Internal-Service-Token`; token bắt buộc trong Docker/prod. Provider mặc định dùng `AI_API_KEY`; khi SYSTEM_ADMIN lưu cấu hình Gemini trong DB, runtime dùng key/model đã lưu. Trợ lý hướng dẫn hệ thống có fallback theo role khi provider tạm thời lỗi. Storage mặc định local profile. Port `8085`; root verify và internal-token/retry tests PASS. Live provider chưa test.
