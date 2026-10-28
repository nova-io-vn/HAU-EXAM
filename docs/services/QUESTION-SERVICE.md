# Question Service

## 1. Mục đích

Quản lý subject, chapter, topic, question, option và review history. Source root `com.questionservice`, database `question_db`.

## 2. Domain/workflow

Question có content, image/storage reference, type, difficulty, facultyId, source, status, creator và options. Workflow là `DRAFT -> PENDING_REVIEW -> APPROVED`; có `NEED_REVISION`, `REJECTED`, `ARCHIVED` theo domain.

## 3. API chính

`GET/POST /api/v1/questions`, `GET/PUT/DELETE /api/v1/questions/{id}`; action `/submit`, `/approve`, `/reject`, `/request-revision`, `/archive`. Catalog subject/chapter/topic nằm dưới `/api/v1/subjects`, `/chapters`, `/topics` với CRUD theo controller.

## 4. Security/integration

USER tạo/sửa/gửi câu hỏi của mình; SUBJECT_ADMIN review và bị giới hạn faculty scope bởi application service. AI completion consumer nhận context reference-based, gọi AI internal result REST bằng `X-Internal-Service-Token`, tạo question source AI ở status DRAFT và chống duplicate.

Phân công câu hỏi cho USER cũng cấp quyền đọc taxonomy và tạo câu hỏi trong môn tương ứng, kể cả phân công đã có trước khi triển khai thay đổi. Quyền này chỉ có hiệu lực khi môn vẫn thuộc faculty hiện tại của USER; kiểm tra `facultyId` và phạm vi cụ thể của phân công vẫn được thực hiện khi tạo câu hỏi. Gán giảng viên trực tiếp vào môn tiếp tục là một cách cấp quyền độc lập.

## 5. Rabbit/Redis/config/test

Publish question workflow events; consume `ai.generation.completed`, có retry/DLQ hữu hạn. Redis không có cache nghiệp vụ được xác nhận trong source hiện tại. Port `8083`; AI URL dev localhost, Docker service name. Root verify PASS; live AI/Rabbit chưa test khi thiếu daemon.
