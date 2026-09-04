# BACKEND MICROSERVICES AGENT SPECIFICATION

> Tài liệu này là **nguồn sự thật chính (Single Source of Truth)** dành cho AI Agent phát triển Backend Microservices của đồ án tốt nghiệp.
>
> Agent phải đọc toàn bộ tài liệu này trước khi tạo, sửa hoặc tái cấu trúc mã nguồn.
>
> **Không được tự ý thay đổi kiến trúc, thêm microservice, đổi database, đổi mô hình xác thực hoặc thay đổi ranh giới nghiệp vụ nếu chưa có yêu cầu rõ ràng.**

---

# 1. Tổng quan đề tài

## 1.1. Tên đề tài

**Nghiên cứu nền tảng quản lý thông điệp RabbitMQ kết hợp AI hiện đại và các kỹ thuật lập trình an toàn trong phát triển hệ thống thông tin hỗ trợ tạo sinh, quản lý ngân hàng đề thi trắc nghiệm cho sinh viên Đại học Kiến trúc Hà Nội.**

## 1.2. Mục tiêu hệ thống

Xây dựng hệ thống Backend theo kiến trúc Microservices hỗ trợ:

- Quản lý người dùng và xác thực.
- Quản lý ngân hàng câu hỏi trắc nghiệm.
- Quản lý ma trận đề thi và bộ đề.
- Upload và xử lý tài liệu.
- AI hỗ trợ tạo sinh câu hỏi.
- Chatbot hỗ trợ khai thác nội dung học liệu.
- Kiểm duyệt câu hỏi theo phạm vi khoa.
- Thông báo realtime, email và thông báo định kỳ.
- Ứng dụng RabbitMQ trong giao tiếp bất đồng bộ.
- Ứng dụng Redis để cache, rate limit, OTP và dữ liệu tạm.
- Áp dụng các kỹ thuật lập trình an toàn trong toàn hệ thống.

## 1.3. Phạm vi

Hệ thống tập trung vào:

- Quản lý học liệu.
- Tạo sinh câu hỏi.
- Quản lý câu hỏi.
- Xét duyệt câu hỏi.
- Quản lý ma trận đề.
- Tạo bộ đề.
- Quản lý thông báo.
- Phân tích dữ liệu bằng AI.

Hệ thống **không phải nền tảng thi trực tuyến hoàn chỉnh**.

Không tập trung vào:

- Sinh viên làm bài trực tuyến.
- Chấm điểm bài thi.
- Giám sát thi.
- Anti-cheat.
- Quản lý phòng thi.

---

# 2. Kiến trúc tổng thể

Hệ thống sử dụng kiến trúc:

**Microservices + Clean Architecture + REST API + Event-Driven Architecture**

Sơ đồ logic:

```text
                         ReactJS
                            |
                            v
                     API Gateway
                            |
       +--------------------+--------------------+
       |                    |                    |
       v                    v                    v
  Auth Service         User Service       Question Service
       |                    |                    |
       |                    |                    |
       +--------------------+--------------------+
                            |
                 REST / RabbitMQ / Redis
                            |
               +------------+-------------+
               |            |             |
               v            v             v
          Exam Service   AI Service   Notification Service
                            |
                         RabbitMQ
                            |
                      External AI APIs

                  Eureka Discovery Server
```

---

# 3. Danh sách 8 Service chính thức

Kiến trúc hiện tại có đúng **8 Spring services**.

## 3.1. Eureka Server

Vai trò:

- Service Discovery.
- Cho các service đăng ký instance.
- Cho phép service tìm nhau bằng service name.
- Theo dõi trạng thái instance.

Không chứa:

- Business logic.
- Authentication.
- Database nghiệp vụ.

---

## 3.2. API Gateway

Vai trò:

- Điểm truy cập duy nhất từ Frontend.
- Expose API ra ngoài.
- Route request.
- Kiểm tra JWT ở mức gateway.
- CORS.
- Rate limiting.
- Correlation ID.
- Logging request cơ bản.

Không được:

- Truy cập trực tiếp database của service.
- Chứa business logic.
- Quyết định quyền nghiệp vụ theo dữ liệu domain.
- Truy vấn Question, Exam hoặc User DB.

Ví dụ:

Gateway có thể biết:

```text
role = SUBJECT_ADMIN
```

Nhưng không được tự quyết định:

```text
Admin này có được duyệt Question X không?
```

Việc đó phải do Question Service xử lý dựa trên faculty scope.

---

## 3.3. Auth Service

### Trách nhiệm

Auth Service chỉ quản lý **Authentication / Credential**.

Chức năng:

- Đăng ký.
- Đăng nhập bằng mã giảng viên và mật khẩu.
- BCrypt password.
- Access Token.
- Refresh Token.
- Refresh token rotation nếu triển khai.
- Logout.
- Change password.
- Forgot password.
- OTP reset password.
- Reset password.
- JWT validation.
- Token revocation/blacklist nếu cần.
- Theo dõi trạng thái credential.

### Không quản lý

Auth Service không quản lý:

- Full name.
- Phone.
- Address.
- Faculty profile.
- Avatar.
- Thông tin hồ sơ cá nhân đầy đủ.

Các dữ liệu trên thuộc User Service.

### Trạng thái tài khoản

```text
PENDING_APPROVAL
ACTIVE
REJECTED
LOCKED
```

Tài khoản đăng ký mới:

```text
Register
  ->
PENDING_APPROVAL
  ->
SYSTEM_ADMIN approve
  ->
ACTIVE
```

Người dùng chỉ được đăng nhập khi trạng thái phù hợp.

### Đăng nhập

Input chính:

```json
{
  "lecturerCode": "GV001",
  "password": "********"
}
```

JWT có thể chứa các claim cần thiết:

```json
{
  "sub": "user-uuid",
  "lecturerCode": "GV001",
  "role": "USER",
  "facultyId": "CNTT",
  "jti": "token-uuid"
}
```

Không đưa dữ liệu nhạy cảm vào JWT.

### Forgot password

Luồng:

```text
Client
  ->
Auth Service
  ->
Generate OTP
  ->
Store OTP/OTP hash in Redis with TTL
  ->
Publish PASSWORD_RESET_OTP_REQUESTED
  ->
RabbitMQ
  ->
Notification Service
  ->
SMTP Email
```

Auth Service **không trực tiếp gửi email**.

---

## 3.4. User Service

### Trách nhiệm

Quản lý:

- Hồ sơ người dùng.
- Vai trò.
- Khoa.
- Trạng thái hồ sơ.
- Thông tin cá nhân.
- Gán role.
- Quản lý account ở góc độ nghiệp vụ.
- Approve tài khoản đăng ký nếu workflow được xử lý tại đây.

### Role

Hệ thống chỉ dùng **Role-Based Access Control**, không triển khai permission matrix phức tạp.

Ba role chính thức:

```text
SYSTEM_ADMIN
SUBJECT_ADMIN
USER
```

### SYSTEM_ADMIN

- Quyền hệ thống cao nhất.
- Quản lý người dùng.
- Duyệt đăng ký.
- Khóa/mở khóa tài khoản.
- Gán role.
- Gán hoặc thay đổi khoa.
- Gán SUBJECT_ADMIN.
- Không tham gia nghiệp vụ kiểm duyệt ngân hàng câu hỏi theo chuyên môn.

### SUBJECT_ADMIN

- Admin chuyên môn.
- Được gán vào một khoa.
- Quản lý ngân hàng câu hỏi thuộc khoa của mình.
- Duyệt/từ chối/yêu cầu sửa câu hỏi thuộc khoa mình.
- Quản lý dữ liệu chuyên môn thuộc phạm vi khoa.
- Không được thao tác dữ liệu chuyên môn thuộc khoa khác.

### USER

- Người tạo nội dung.
- Upload tài liệu.
- Tạo câu hỏi thủ công.
- Yêu cầu AI tạo sinh câu hỏi.
- Chỉnh sửa câu hỏi của mình.
- Gửi câu hỏi xét duyệt.
- Theo dõi trạng thái.

### Data Scope

Ngoài RBAC, hệ thống có **Faculty Data Scope**.

Ví dụ:

```text
SUBJECT_ADMIN.facultyId = CNTT
Question.facultyId = CNTT
=> được phép duyệt
```

Nhưng:

```text
SUBJECT_ADMIN.facultyId = KIENTRUC
Question.facultyId = CNTT
=> 403 Forbidden
```

### Dữ liệu chính

Không lưu `age` trực tiếp.

Nên lưu:

```text
id
lecturerCode
fullName
dateOfBirth
phone
email
address
avatar
facultyId
role
status
createdAt
updatedAt
```

Tuổi được tính từ `dateOfBirth`.

---

## 3.5. Question Service

### Trách nhiệm

Question Service là trung tâm của ngân hàng câu hỏi.

Quản lý:

- Subject.
- Chapter.
- Topic.
- Question.
- Question Option.
- Difficulty.
- Question Type.
- Question Status.
- Question source.
- AI generated flag.
- Created by.
- Reviewed by.
- Approval history.
- Revision request.
- Filter/search/statistics liên quan câu hỏi.

### Database

Ưu tiên:

**PostgreSQL**

Lý do:

- Dữ liệu có cấu trúc rõ.
- Có nhiều quan hệ.
- Có filter và thống kê.
- Có workflow approval.
- Có liên kết logic với subject/chapter/topic/exam.
- Dễ biểu diễn trực quan ERD trong pgAdmin.

### Ảnh trong câu hỏi

Không lưu binary ảnh trực tiếp trong PostgreSQL.

DB chỉ lưu:

```text
imageUrl
```

File được lưu ở:

- Local storage trong development.
- MinIO/S3-compatible storage nếu triển khai nâng cao.

Question và QuestionOption đều có thể có `imageUrl`.

### Workflow

```text
DRAFT
  |
  v
PENDING_REVIEW
  |
  +------------------+
  |                  |
  v                  v
APPROVED        NEED_REVISION
                    |
                    v
                  DRAFT
```

Có thể hỗ trợ thêm:

```text
REJECTED
ARCHIVED
```

### Quy tắc quan trọng

SUBJECT_ADMIN chỉ được review câu hỏi thuộc `facultyId` mà mình quản lý.

---

## 3.6. Exam Service

Exam Service không phải hệ thống thi online.

### Trách nhiệm

Quản lý:

- Exam Matrix.
- Exam Template.
- Exam Version.
- Question Distribution.
- Difficulty Distribution.
- Topic Distribution.
- Random/select question.
- Sinh bộ đề từ ngân hàng câu hỏi đã duyệt.
- Kiểm tra độ phủ của đề.
- Export đề nếu có.

Ví dụ ma trận:

| Chapter | EASY | MEDIUM | HARD | Total |
|---|---:|---:|---:|---:|
| Chapter 1 | 5 | 3 | 2 | 10 |
| Chapter 2 | 4 | 4 | 2 | 10 |
| Chapter 3 | 3 | 5 | 2 | 10 |

Exam Service lấy Question thông qua API/Event Contract, **không truy cập Question DB**.

---

## 3.7. AI Service

AI Service là trung tâm xử lý AI và tài liệu.

Không tách AI Worker thành microservice riêng.

### Các module nội bộ

```text
AI Service
|
+-- Document
|   +-- upload
|   +-- extract
|   +-- parse
|
+-- Generation
|   +-- question
|   +-- answer
|   +-- distractor
|   +-- explanation
|
+-- Analysis
|   +-- difficulty
|   +-- topic classification
|   +-- knowledge coverage
|
+-- Chatbot
|
+-- Job
    +-- PENDING
    +-- PROCESSING
    +-- COMPLETED
    +-- FAILED
```

### Chức năng

- Upload tài liệu.
- Đọc/trích xuất tài liệu.
- Chuẩn hóa nội dung.
- Gọi AI provider.
- Sinh câu hỏi.
- Sinh đáp án.
- Sinh phương án nhiễu.
- Phân tích độ khó.
- Phân loại topic.
- Phân tích coverage.
- Chatbot.
- Theo dõi AI job.

### Xử lý tác vụ nặng

Không để HTTP request chờ AI xử lý lâu.

Flow:

```text
POST generate
   ->
Create job
   ->
Return 202 + jobId
   ->
RabbitMQ
   ->
AI processing
   ->
Publish result event
```

Response:

```json
{
  "jobId": "uuid",
  "status": "PROCESSING"
}
```

### Kết quả AI

AI phải ưu tiên trả dữ liệu có cấu trúc theo JSON schema.

Ví dụ:

```json
{
  "question": "...",
  "options": [
    {
      "label": "A",
      "content": "..."
    }
  ],
  "correctAnswer": "A",
  "difficulty": "MEDIUM",
  "topicId": "...",
  "explanation": "..."
}
```

Không lưu trực tiếp output text không kiểm soát nếu nghiệp vụ cần object có cấu trúc.

---

## 3.8. Notification Service

Notification Service là **trung tâm thông báo**.

### Không coi Socket / Email / Scheduled là ba loại nghiệp vụ độc lập

Chúng là **Notification Delivery Channels**.

Một notification có thể được gửi qua một hoặc nhiều channel:

```text
IN_APP
WEBSOCKET
EMAIL
```

### Realtime notification

Dùng:

```text
Spring WebSocket
STOMP
```

Flow:

```text
Domain Service
  ->
RabbitMQ Event
  ->
Notification Service
  ->
Save notification
  ->
WebSocket
  ->
ReactJS
```

Frontend nên subscribe kiểu:

```text
/user/queue/notifications
```

Không nên expose topic trực tiếp theo userId nếu không cần.

### Email notification

Service khác **không gửi SMTP trực tiếp**.

Flow:

```text
Auth / Question / AI
  ->
RabbitMQ
  ->
Notification Service
  ->
SMTP
```

### Scheduled/System notification

Notification Service có DB.

Dùng:

```text
Spring @Scheduled
```

trong phiên bản đầu.

Có thể nâng cấp Quartz sau nếu thực sự cần.

Ví dụ entity:

```text
ScheduledNotification
id
title
content
targetRole
targetFaculty
scheduledAt
status
createdBy
createdAt
```

---

# 4. Clean Architecture bắt buộc cho từng Microservice

Mỗi service nghiệp vụ phải tổ chức theo 4 layer chính:

```text
domain
application
infrastructure
presentation
```

Cấu trúc tham khảo:

```text
service-name/
└── src/main/java/com/hau/<service>/
    |
    +-- domain/
    |   +-- model/
    |   +-- enum/
    |   +-- repository/
    |   +-- service/
    |   +-- exception/
    |
    +-- application/
    |   +-- usecase/
    |   +-- command/
    |   +-- query/
    |   +-- dto/
    |   +-- mapper/
    |   +-- port/
    |       +-- in/
    |       +-- out/
    |
    +-- infrastructure/
    |   +-- persistence/
    |   |   +-- entity/
    |   |   +-- repository/
    |   |   +-- adapter/
    |   |   +-- mapper/
    |   |
    |   +-- rabbitmq/
    |   +-- redis/
    |   +-- security/
    |   +-- mail/
    |   +-- external/
    |   +-- config/
    |
    +-- presentation/
        +-- controller/
        +-- request/
        +-- response/
        +-- advice/
```

Không bắt buộc service nào cũng phải có mọi thư mục con.

Chỉ tạo thư mục khi thật sự có trách nhiệm tương ứng.

---

# 5. Dependency Rule của Clean Architecture

Luồng phụ thuộc:

```text
Presentation
     |
     v
Application
     |
     v
Domain

Infrastructure
     |
     +-- implements Application/Domain ports
```

### Domain không được phụ thuộc

Domain không được biết trực tiếp:

- Spring MVC.
- Spring Data JPA.
- RabbitMQ.
- Redis.
- PostgreSQL.
- WebSocket.
- Jackson.
- Gemini.
- SMTP.
- JWT library.

### Presentation

Chỉ làm:

- Nhận HTTP request.
- Validate request format.
- Chuyển request vào use case.
- Trả response.

Không chứa business logic.

### Application

Chứa:

- Use case.
- Application orchestration.
- Transaction boundary phù hợp.
- Port.
- DTO nội bộ nếu cần.

### Infrastructure

Chứa implementation cho:

- JPA.
- Redis.
- RabbitMQ.
- SMTP.
- External API.
- JWT.
- File storage.

---

# 6. REST API Convention

Dùng REST API làm giao tiếp đồng bộ.

Ví dụ:

```text
GET    /api/v1/questions
GET    /api/v1/questions/{id}
POST   /api/v1/questions
PUT    /api/v1/questions/{id}
DELETE /api/v1/questions/{id}
```

Action nghiệp vụ có thể dùng:

```text
POST /api/v1/questions/{id}/submit
POST /api/v1/questions/{id}/approve
POST /api/v1/questions/{id}/request-revision
POST /api/v1/questions/{id}/archive
```

---

# 7. Chuẩn JSON API

Tất cả service phải thống nhất format response.

## Success

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Operation successful",
  "data": {}
}
```

## Error

```json
{
  "success": false,
  "code": "QUESTION_NOT_FOUND",
  "message": "Question not found",
  "data": null
}
```

Có thể thêm:

```text
timestamp
path
correlationId
errors
```

khi phù hợp.

---

# 8. JSON Conversion và Mapper

## Jackson

Dùng Jackson cho:

```text
JSON <-> Java DTO
JSON <-> RabbitMQ Event
```

## MapStruct

Dùng MapStruct cho:

```text
Presentation DTO <-> Application/Domain
Domain <-> Persistence Entity
```

Không dùng JPA Entity làm REST Response.

Không dùng JPA Entity làm RabbitMQ Message.

---

# 9. DTO Rule

Phải tách:

```text
Request DTO
Response DTO
Event DTO
Persistence Entity
Domain Model
```

Không dùng một class cho tất cả mục đích.

Ví dụ không được:

```text
UserEntity
  ->
Controller Response
  ->
RabbitMQ Message
```

---

# 10. RabbitMQ Architecture

RabbitMQ chỉ dùng cho tác vụ bất đồng bộ / event-driven.

### REST dùng khi

- Cần kết quả ngay.
- Query dữ liệu.
- Command đơn giản cần response trực tiếp.

### RabbitMQ dùng khi

- AI processing.
- Email notification.
- Domain event.
- Background processing.
- Tác vụ nặng.
- Service không cần chờ kết quả ngay.

---

# 11. RabbitMQ Exchange

Ưu tiên Topic Exchange theo domain.

Ví dụ:

```text
auth.exchange
user.exchange
question.exchange
exam.exchange
ai.exchange
notification.exchange
```

Routing key:

```text
user.registration.requested
user.approved
user.role.changed

question.created
question.submitted
question.approved
question.rejected
question.revision.requested

ai.generation.requested
ai.generation.completed
ai.generation.failed

exam.generated

notification.requested
```

---

# 12. Event Envelope chuẩn

RabbitMQ event phải có metadata chuẩn.

```json
{
  "eventId": "uuid",
  "eventType": "QUESTION_APPROVED",
  "correlationId": "uuid",
  "occurredAt": "2026-09-04T21:30:00+07:00",
  "version": 1,
  "payload": {}
}
```

Bắt buộc cân nhắc:

- eventId.
- correlationId.
- eventType.
- occurredAt.
- version.
- payload.

---

# 13. ACK / NACK

Không ACK trước khi business processing hoàn tất.

Luồng:

```text
Receive
  ->
Process
  ->
Success
  ->
ACK
```

Lỗi:

```text
Receive
  ->
Process Failed
  ->
NACK / Retry mechanism
```

---

# 14. Retry và Dead Letter Queue

Các queue quan trọng phải cân nhắc:

```text
main queue
retry queue
dead-letter queue
```

Ví dụ:

```text
ai.question.generate.queue
ai.question.generate.retry.queue
ai.question.generate.dlq
```

Không retry vô hạn.

Retry phải:

- Giới hạn số lần.
- Có delay/backoff.
- Sau giới hạn chuyển DLQ.

---

# 15. Idempotency

RabbitMQ có thể delivery lại message.

Consumer phải được thiết kế idempotent cho các event quan trọng.

Sử dụng:

```text
eventId
jobId
correlationId
```

Ví dụ:

```text
eventId đã xử lý?
YES -> ACK + skip
NO  -> process
```

Không được tạo duplicate Question chỉ vì message được redeliver.

---

# 16. Redis Strategy

Redis không phải source of truth.

Source of truth chính vẫn là PostgreSQL hoặc storage tương ứng.

Redis dùng cho dữ liệu:

- Tạm thời.
- Đọc nhiều.
- Có thể tái tạo.
- Có TTL.
- Rate limit.
- OTP.
- Cache.

---

# 17. Redis Cache Aside Pattern

Pattern mặc định:

```text
Request
  ->
Check Redis
  ->
HIT -> return
  ->
MISS
  ->
Database
  ->
Put Redis with TTL
  ->
return
```

Update:

```text
Update DB
  ->
Evict / refresh cache
```

---

# 18. Redis Usage theo Service

## Auth

- OTP.
- Login attempt.
- Rate limiting.
- JWT blacklist nếu triển khai.
- Temporary security state.

## User

Có thể cache:

- User profile.
- Role.
- Faculty metadata.

Chỉ thêm nếu có giá trị thực tế.

## Question

Có thể cache:

- Question detail.
- Subject.
- Chapter.
- Topic.
- Dashboard statistics.
- Các query read-heavy.

## Exam

Có thể cache:

- Exam matrix.
- Generated temporary exam.
- Kết quả lựa chọn câu hỏi tạm thời.

## AI

Có thể dùng:

- AI job state.
- Temporary progress.
- Temporary extracted metadata.

## Notification

Redis không bắt buộc trong phiên bản đầu.

## Gateway

Redis có thể dùng:

- Rate limiting.

---

# 19. Redis TTL

Không cache vô thời hạn.

TTL phải được chọn theo bản chất dữ liệu.

Ví dụ tham khảo:

```text
profile            5-15 phút
subject            30 phút
question detail    5-10 phút
dashboard stats    1-5 phút
OTP                khoảng 5 phút
```

Agent phải coi đây là tham khảo, không hard-code nếu chưa có quyết định chính thức.

---

# 20. WebSocket

Notification Service sử dụng:

```text
Spring WebSocket
STOMP
```

WebSocket dùng cho:

```text
Server -> Browser realtime
```

RabbitMQ dùng cho:

```text
Service -> Service
```

Không được nhầm hai trách nhiệm này.

Flow chuẩn:

```text
Question Service
  ->
RabbitMQ
  ->
Notification Service
  ->
WebSocket
  ->
ReactJS
```

---

# 21. Notification Delivery Strategy

Notification có thể có:

```text
IN_APP
WEBSOCKET
EMAIL
```

Ví dụ:

### QUESTION_APPROVED

```text
Save DB   = YES
WebSocket = YES
Email     = optional
```

### PASSWORD_RESET_OTP

```text
Save DB   = optional/no
WebSocket = NO
Email     = YES
```

### SYSTEM_MAINTENANCE

```text
Save DB   = YES
WebSocket = YES
Email     = configurable
```

---

# 22. Notification Persistence

Entity tham khảo:

```text
Notification
id
userId
type
title
content
referenceId
referenceType
isRead
createdAt
```

Scheduled notification:

```text
ScheduledNotification
id
title
content
targetRole
targetFaculty
scheduledAt
status
createdBy
createdAt
```

---

# 23. Scheduler

Phiên bản đầu dùng:

```text
Spring @Scheduled
```

Không thêm Quartz nếu chưa có yêu cầu nghiệp vụ thật sự.

---

# 24. Database Strategy

Áp dụng:

**Database per Service**

Nguyên tắc:

```text
Auth Service     -> Auth DB
User Service     -> User DB
Question Service -> Question DB
Exam Service     -> Exam DB
AI Service       -> AI DB / file storage
Notification     -> Notification DB
```

Có thể cùng một PostgreSQL server trong development nhưng phải tách database/schema ownership logic rõ ràng.

---

# 25. Không truy cập DB chéo Service

Tuyệt đối không:

```text
Question Service
   ->
User Database
```

Phải dùng:

```text
Question Service
   ->
REST
   ->
User Service
```

hoặc Event:

```text
User Service
   ->
RabbitMQ
   ->
Question Service
```

ID xuyên service chỉ là **logical reference**, không tạo foreign key xuyên database service.

---

# 26. PostgreSQL

Ưu tiên PostgreSQL cho:

- Auth.
- User.
- Question.
- Exam.
- Notification.

Question Service dùng PostgreSQL thay vì MongoDB trong kiến trúc hiện tại.

---

# 27. File Storage

Ảnh/PDF không nên lưu binary trực tiếp trong relational database nếu không có lý do đặc biệt.

DB giữ:

```text
fileUrl
imageUrl
storageKey
metadata
```

Storage:

- Local storage cho development.
- MinIO/S3-compatible storage nếu triển khai.

---

# 28. Flyway

Mỗi service sở hữu database phải dùng migration.

Ưu tiên:

```text
Flyway
```

Migration:

```text
V1__init_schema.sql
V2__add_xxx.sql
V3__alter_xxx.sql
```

Không sửa migration đã chạy trên môi trường thật; tạo migration mới.

---

# 29. Security

Backend phải áp dụng:

- Spring Security.
- JWT.
- Access Token.
- Refresh Token.
- BCrypt.
- RBAC.
- Faculty Data Scope.
- Input Validation.
- CORS.
- Rate Limit.
- Secure logging.
- Safe exception response.
- Parameterized queries/JPA.
- Không hard-code secret.
- Không log password/token/OTP.

---

# 30. JWT

JWT phải được ký và kiểm tra hợp lệ.

Không mô tả JWT là cơ chế "mã hóa toàn bộ dữ liệu".

JWT payload có thể đọc được nếu không dùng JWE.

Do đó:

- Không chứa password.
- Không chứa OTP.
- Không chứa dữ liệu nhạy cảm không cần thiết.

---

# 31. Authorization

Có hai lớp:

## Role Scope

```text
SYSTEM_ADMIN
SUBJECT_ADMIN
USER
```

## Faculty Data Scope

Ví dụ SUBJECT_ADMIN phải được kiểm tra faculty ownership ở service nghiệp vụ.

Không chỉ dựa vào role.

---

# 32. Logging

Sử dụng:

```text
SLF4J + Logback
```

Không dùng:

```java
System.out.println(...)
```

Log cần phù hợp level:

```text
TRACE
DEBUG
INFO
WARN
ERROR
```

Không log dữ liệu nhạy cảm.

---

# 33. Correlation ID

Gateway tạo hoặc forward:

```text
X-Correlation-Id
```

ID này cần được truyền qua:

- REST.
- RabbitMQ event.
- Logging context nếu có thể.

Ví dụ:

```text
[correlationId=abc123] Question submitted
[correlationId=abc123] Notification published
```

---

# 34. Global Exception Handling

Không để exception stacktrace/raw framework error trả trực tiếp cho frontend.

Presentation phải có Global Exception Handler.

Phân loại:

- Validation errors.
- Domain exception.
- Resource not found.
- Conflict.
- Unauthorized.
- Forbidden.
- External API failure.
- Infrastructure failure.
- Unexpected error.

Response theo chuẩn JSON API.

---

# 35. Validation

Dùng Jakarta Validation cho Request DTO.

Ví dụ:

```text
@NotBlank
@NotNull
@Email
@Size
@Pattern
@Min
@Max
```

Business validation vẫn phải nằm ở Domain/Application.

Không coi annotation validation là thay thế cho business rule.

---

# 36. API Documentation

Dùng:

```text
OpenAPI / Swagger
```

Mỗi API cần có:

- Method.
- Path.
- Request.
- Response.
- Error codes.
- Auth requirement.
- Role requirement.

---

# 37. Coding Convention

Backend chuẩn:

```text
Java 21
Spring Boot
Spring Security
Spring Data JPA
PostgreSQL
Flyway
RabbitMQ
Redis
Jackson
MapStruct
Jakarta Validation
SLF4J
OpenAPI
```

Version cụ thể phải được khóa trong build file của project, không tự ý nâng version trong lúc implement service khác.

---

# 38. Dependency Injection

Chỉ dùng:

**Constructor Injection**

Không dùng:

```java
@Autowired
private XService xService;
```

Ưu tiên:

```java
@RequiredArgsConstructor
```

hoặc constructor explicit.

---

# 39. Entity Rule

JPA Entity thuộc Infrastructure Persistence.

Không expose Entity ra:

- Controller.
- REST contract.
- RabbitMQ event.

---

# 40. Repository Rule

Domain/Application phụ thuộc abstraction.

Infrastructure implement abstraction.

Không để use case bị khóa trực tiếp vào chi tiết persistence nếu có thể tránh.

---

# 41. Transaction

Transaction đặt ở Application/Infrastructure boundary phù hợp.

Không đặt `@Transactional` tùy tiện ở Controller.

Không giữ transaction DB mở trong khi gọi AI API lâu hoặc chờ network nếu có thể tránh.

---

# 42. External API

AI provider, SMTP, storage... phải được đóng gói qua Port/Adapter.

Ví dụ:

```text
application.port.out.AiProvider
              ^
              |
infrastructure.external.GeminiAdapter
```

Không gọi Gemini SDK trực tiếp từ Controller.

---

# 43. AI Error Handling

Phải xử lý:

- Timeout.
- Rate limit.
- Invalid JSON.
- Provider unavailable.
- Malformed output.
- Empty output.
- Retry phù hợp.
- DLQ nếu async message thất bại nhiều lần.

AI output phải được validate trước khi đưa sang Question Service.

---

# 44. Async Job Pattern

Các task AI dài phải dùng Job.

Entity/status tham khảo:

```text
PENDING
PROCESSING
COMPLETED
FAILED
```

Có thể thêm:

```text
RETRYING
CANCELLED
```

nếu thực sự cần.

---

# 45. Response Code cho Async

Khi request chỉ bắt đầu background job:

Ưu tiên:

```text
HTTP 202 Accepted
```

và trả:

```json
{
  "success": true,
  "code": "AI_JOB_ACCEPTED",
  "message": "AI job accepted",
  "data": {
    "jobId": "uuid",
    "status": "PROCESSING"
  }
}
```

---

# 46. Service Communication Rule

Ưu tiên:

```text
REST        -> synchronous query/command
RabbitMQ    -> asynchronous event/background
WebSocket   -> realtime server-to-client
Redis       -> cache/temporary state/rate limit
```

Không dùng công nghệ sai mục đích chỉ để tăng độ phức tạp.

---

# 47. Service Boundary Rule

Agent không được:

- Tạo File Service mới.
- Tạo AI Worker Service mới.
- Tạo Permission Service.
- Tạo Academic Service.
- Gộp Auth và User.
- Gộp Question và Exam nếu chưa có yêu cầu mới.
- Thêm Keycloak.

Kiến trúc 8 service hiện tại là cố định.

---

# 48. Keycloak

**Không sử dụng Keycloak.**

Authentication được xây dựng nội bộ bởi Auth Service.

Không tự ý thêm:

- Keycloak.
- Auth0.
- Firebase Auth.
- External Identity Provider.

trừ khi có yêu cầu mới rõ ràng.

---

# 49. AI Worker

**Không có AI Worker microservice riêng.**

Worker/consumer xử lý async nằm bên trong AI Service.

Có thể tổ chức package/module worker nội bộ:

```text
ai-service/
└── infrastructure/
    └── rabbitmq/
        └── consumer/
```

nhưng nó vẫn là cùng một deployable service.

---

# 50. API Gateway Security

Gateway có thể:

- Validate JWT.
- Reject token không hợp lệ.
- Rate limit.
- Forward claims/header cần thiết.

Service nghiệp vụ vẫn phải bảo vệ endpoint quan trọng.

Không được coi Gateway là lớp security duy nhất nếu service có khả năng bị truy cập nội bộ.

---

# 51. Business Rule quan trọng

Một số rule đã chốt:

1. User đăng ký phải chờ duyệt.
2. SYSTEM_ADMIN quản lý account/role.
3. SYSTEM_ADMIN không phải người xét duyệt chuyên môn.
4. SUBJECT_ADMIN quản lý nội dung theo khoa được phân công.
5. SUBJECT_ADMIN không thao tác nội dung khoa khác.
6. USER tạo câu hỏi/upload tài liệu/yêu cầu AI.
7. AI-generated question phải qua workflow review nếu nghiệp vụ yêu cầu.
8. Question dùng PostgreSQL.
9. Exam quản lý ma trận/bộ đề, không tổ chức thi online.
10. Notification là trung tâm duy nhất cho email/realtime/system notification.

---

# 52. Suggested Event Flow: Registration

```text
Client
  ->
Auth Service
  ->
Create credential PENDING_APPROVAL
  ->
user.registration.requested
  ->
RabbitMQ
  ->
User Service
  ->
Create user profile
```

Approve:

```text
SYSTEM_ADMIN
  ->
User Service
  ->
Approve user
  ->
user.approved
  ->
RabbitMQ
  ->
Auth Service
  ->
Credential ACTIVE
```

Agent phải chú ý consistency và idempotency trong flow này.

---

# 53. Suggested Event Flow: Question Approval

```text
USER
  ->
Question Service
  ->
Submit Question
  ->
PENDING_REVIEW

SUBJECT_ADMIN
  ->
Question Service
  ->
Check faculty scope
  ->
Approve
  ->
question.approved
  ->
RabbitMQ
  ->
Notification Service
  ->
DB + WebSocket
```

---

# 54. Suggested Event Flow: AI Generation

```text
USER
  ->
AI Service
  ->
Create AI Job
  ->
Return 202
  ->
RabbitMQ
  ->
AI consumer
  ->
Extract document
  ->
Generate questions
  ->
Validate structured result
  ->
ai.generation.completed
  ->
RabbitMQ
  ->
Question Service
  ->
Store generated questions
  ->
notification event
  ->
Notification Service
  ->
WebSocket
```

---

# 55. Suggested Event Flow: Forgot Password

```text
USER
  ->
Auth Service
  ->
Generate OTP
  ->
Redis TTL
  ->
password.reset.otp.requested
  ->
RabbitMQ
  ->
Notification Service
  ->
Email
```

Verify:

```text
USER
  ->
Auth Service
  ->
Check OTP
  ->
Reset password
  ->
Invalidate OTP
```

---

# 56. Testing Requirements

Mỗi service cần cân nhắc:

- Unit Test.
- Application Use Case Test.
- Repository Integration Test.
- Controller/API Test.
- Security Test.
- RabbitMQ Consumer Test.
- Redis behavior test nếu dùng.
- Validation Test.

Không nhất thiết đạt coverage 100%.

Ưu tiên test business rule quan trọng.

---

# 57. Security Testing

Cần kiểm tra:

- Unauthorized request.
- Forbidden role.
- Faculty scope violation.
- Invalid JWT.
- Expired JWT.
- Reused refresh token nếu rotation.
- Invalid OTP.
- Brute force login/rate limiting.
- Invalid input.
- Injection payload.
- Unauthorized WebSocket subscription.

---

# 58. RabbitMQ Testing

Test:

- Producer publish đúng routing key.
- Consumer nhận đúng event.
- Retry hoạt động.
- DLQ hoạt động.
- Duplicate event không tạo duplicate business data.
- Correlation ID được giữ.
- Service restart không làm mất message đã chưa ACK.

---

# 59. Redis Testing

Test:

- Cache HIT.
- Cache MISS.
- TTL.
- Cache eviction sau update.
- OTP hết hạn.
- Rate limiting.
- Không sử dụng stale cache sai nghiệp vụ.

---

# 60. WebSocket Testing

Test:

- User authenticated mới subscribe.
- User chỉ nhận notification của mình.
- Connection reconnect.
- Notification được persist trước/đồng thời với realtime theo thiết kế.
- Không expose notification người khác.

---

# 61. Docker

Mỗi service phải có khả năng container hóa.

Hệ thống development có thể dùng Docker Compose cho:

- PostgreSQL.
- Redis.
- RabbitMQ.
- Eureka.
- Gateway.
- Business services.
- MinIO nếu sử dụng.

Không hard-code `localhost` trong config production/container.

---

# 62. Configuration

Ưu tiên environment variables.

Ví dụ:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
REDIS_HOST
RABBITMQ_HOST
SMTP_HOST
AI_API_KEY
```

Không commit secret thật vào Git.

---

# 63. CI/CD

Mục tiêu:

```text
GitHub Actions
```

Pipeline cơ bản:

```text
checkout
  ->
build
  ->
test
  ->
package
  ->
docker build
```

Deploy chỉ thêm khi môi trường được chốt.

---

# 64. Definition of Done cho một Service

Một service chỉ được coi là hoàn chỉnh khi các phần có liên quan đã được xử lý:

```text
[ ] Business responsibility rõ
[ ] Out-of-scope rõ
[ ] Clean Architecture đúng
[ ] Domain model
[ ] Use cases
[ ] API contract
[ ] Persistence
[ ] Flyway migration
[ ] DTO + Mapper
[ ] Validation
[ ] Global exception handling
[ ] Security
[ ] Logging
[ ] Correlation ID
[ ] RabbitMQ nếu service cần
[ ] Redis nếu service cần
[ ] Swagger/OpenAPI
[ ] Tests quan trọng
[ ] Docker build
[ ] README/run instructions
```

Không bắt buộc tick mục không áp dụng.

---

# 65. Quy trình làm việc bắt buộc cho AI Agent

Khi được yêu cầu xây một service, Agent phải làm theo thứ tự:

## Step 1 - Phân tích nghiệp vụ

Trình bày:

- Service làm gì.
- Không làm gì.
- Actor.
- Use case.
- Business rule.

## Step 2 - Phân tích data model

Chốt:

- Domain model.
- Entity persistence.
- Enum.
- Relation.
- Index.
- Constraint.

## Step 3 - API Contract

Chốt:

- Endpoint.
- Request.
- Response.
- Error.
- Role.

## Step 4 - Async Contract

Nếu có:

- Exchange.
- Queue.
- Routing key.
- Event schema.
- Producer.
- Consumer.
- Retry.
- DLQ.
- Idempotency.

## Step 5 - Redis

Nếu có:

- Key.
- TTL.
- Invalidation.
- Cache pattern.

## Step 6 - Clean Architecture

Liệt kê file/package sẽ tạo.

## Step 7 - Implementation

Code từng nhóm chức năng.

Không dump toàn bộ project một lần nếu task lớn.

## Step 8 - Test

Cung cấp test chính và cách test API.

---

# 66. Cách Agent xử lý khi phát hiện thiết kế có vấn đề

Agent không được âm thầm thay kiến trúc.

Nếu phát hiện:

- Circular dependency.
- Sai ownership.
- Duplicate responsibility.
- Security issue.
- Consistency issue.
- Event design dễ duplicate.
- Cache invalidation sai.
- Business rule chưa rõ.

Agent phải:

1. Chỉ ra vấn đề.
2. Giải thích ảnh hưởng.
3. Đề xuất phương án sửa tối thiểu.
4. Không tự ý thêm service hoặc thay kiến trúc lớn.

---

# 67. Những điều Agent tuyệt đối không được làm

Không được:

- Dùng Keycloak.
- Tạo AI Worker microservice riêng.
- Tạo permission system nếu chưa được yêu cầu.
- Cho service truy cập DB service khác.
- Trả Entity trực tiếp từ Controller.
- Gửi Entity qua RabbitMQ.
- Gửi password/token/OTP vào log.
- Lưu plain-text password.
- Hard-code secret.
- Đặt business logic trong Controller.
- Dùng field injection.
- Dùng System.out.println làm logging.
- Retry RabbitMQ vô hạn.
- Cache dữ liệu tùy tiện không có invalidation.
- Dùng Redis làm source of truth.
- Cho SUBJECT_ADMIN thao tác khoa khác.
- Cho SYSTEM_ADMIN tự động trở thành admin chuyên môn.
- Để Gateway xử lý business authorization theo Question/Exam ownership.
- Dùng HTTP request dài để chờ AI hoàn tất nếu có thể xử lý async.
- Tự ý nâng version dependency trên từng service.

---

# 68. Nguyên tắc ưu tiên thiết kế

Khi có nhiều lựa chọn, ưu tiên theo thứ tự:

1. Đúng nghiệp vụ.
2. An toàn.
3. Dễ bảo trì.
4. Ranh giới service rõ.
5. Dễ test.
6. Có khả năng mở rộng.
7. Dễ trình bày trong đồ án.
8. Tránh over-engineering.

Không chọn giải pháp phức tạp chỉ để thể hiện nhiều công nghệ.

---

# 69. Nguyên tắc cuối cùng

Mỗi công nghệ phải có lý do tồn tại:

```text
Eureka      -> Discovery
Gateway     -> API entry/routing/security
REST        -> Synchronous communication
RabbitMQ    -> Async/event-driven communication
Redis       -> Cache/OTP/rate limit/temp state
WebSocket   -> Realtime client notification
PostgreSQL  -> Persistent structured business data
Flyway      -> DB migration
Jackson     -> JSON conversion
MapStruct   -> Layer mapping
JWT         -> Stateless authentication token
BCrypt      -> Password hashing
AI Service  -> Document/AI processing
```

Nếu một công nghệ không giải quyết một vấn đề thật, không thêm vào chỉ để làm hệ thống phức tạp hơn.

---

# 70. Prompt khởi động Agent

Có thể dùng prompt sau cùng với tài liệu này:

```text
Bạn là Senior Backend Microservices Engineer phụ trách đồ án tốt nghiệp này.

Trước khi thực hiện bất kỳ thay đổi nào, hãy đọc toàn bộ file BACKEND_MICROSERVICES_AGENT.md và coi đây là Single Source of Truth về kiến trúc.

Yêu cầu:
- Tuân thủ đúng 8 service đã chốt.
- Dùng Clean Architecture: Domain, Application, Infrastructure, Presentation.
- Không sử dụng Keycloak.
- Không tạo AI Worker service riêng.
- Không truy cập database chéo service.
- REST cho synchronous communication.
- RabbitMQ cho async/event-driven communication.
- Redis chỉ dùng cho cache, OTP, rate limit hoặc temporary state phù hợp.
- Notification Service là trung tâm notification, WebSocket và email.
- Jackson dùng chuyển JSON.
- MapStruct dùng mapping giữa các layer.
- PostgreSQL là database chính cho dữ liệu nghiệp vụ có cấu trúc.
- JWT + Spring Security cho authentication/authorization.
- Chỉ dùng 3 role: SYSTEM_ADMIN, SUBJECT_ADMIN, USER.
- SUBJECT_ADMIN bị giới hạn dữ liệu theo faculty.
- Không tự ý thay đổi kiến trúc nếu chưa được yêu cầu.

Khi bắt đầu một service:
1. Phân tích nghiệp vụ.
2. Chỉ ra lỗi hoặc điểm chưa hợp lý nếu có.
3. Chốt entity/domain model.
4. Chốt API.
5. Chốt RabbitMQ event nếu có.
6. Chốt Redis strategy nếu có.
7. Chốt cấu trúc file Clean Architecture.
8. Sau đó mới bắt đầu viết code từng phần.

Không tạo toàn bộ project trong một lần nếu task lớn.
Ưu tiên giải pháp rõ ràng, an toàn, dễ kiểm thử và phù hợp đồ án tốt nghiệp.
```

---

# BUILD BASELINE - BẮT BUỘC

Các thông số nền tảng sau đã được khóa cho toàn bộ Backend:

```text
Java            = 21
Build Tool      = Maven
Maven           = 3.9+ recommended
Spring Boot     = 4.1.1
Spring Cloud    = 2025.1.3
Configuration   = application.yml
Packaging       = jar
Architecture    = Maven Multi-Module Microservices
```

## Maven Rules

- Tất cả service dùng `pom.xml`.
- Không tạo `build.gradle`.
- Root project có parent/aggregator `pom.xml`.
- Spring Boot version được khóa tại root.
- Spring Cloud BOM được khóa tại root.
- Không tự ý đặt version Spring khác nhau giữa các service.
- Dependency dùng version do Spring Boot/Spring Cloud BOM quản lý nếu có thể.
- Chỉ khai báo version riêng cho thư viện ngoài BOM khi thật sự cần.

## application.yml Rules

- Tất cả service dùng `src/main/resources/application.yml`.
- Không chuyển sang `application.properties`.
- Secret không hard-code trong YAML.
- Dùng environment variable với default chỉ dành cho local development.
- Mỗi service có `spring.application.name` duy nhất.
- `eureka.client.service-url.defaultZone` phải dùng đúng camel-case `defaultZone`.
- Cấu hình môi trường production có thể tách bằng Spring Profile sau này, ví dụ:
  `application-dev.yml`, `application-docker.yml`, `application-prod.yml`.

## Port Convention

```text
eureka-server         8761
api-gateway           8080
auth-service          8081
user-service          8082
question-service      8083
exam-service          8084
ai-service            8085
notification-service  8086
```

## Base Repository

Agent phải coi cấu trúc `hau-exam-bank-backend/` là base chính thức.

Không tạo lại project từ Spring Initializr nếu base đã tồn tại.

Khi implement một service:
1. Giữ parent Maven hiện tại.
2. Chỉ thêm dependency cần cho service đó.
3. Giữ Clean Architecture 4 layer.
4. Giữ `application.yml`.
5. Không đổi port/service-name nếu chưa được yêu cầu.


Bộ tài liệu này được đặt vào repository theo đúng cấu trúc thư mục hiện
tại.

Thứ tự Agent nên đọc:

1.  `/AGENTS.md`
2.  `/backend/AGENTS.md` hoặc `/frontend/AGENTS.md`
3.  `AGENTS.md` gần service/feature đang sửa nhất
4.  `docs/BUSINESS.md`
5.  `docs/API.md`
6.  `docs/DATABASE.md`
7.  `docs/EVENTS.md`

`AGENTS.md` là luật làm việc. `docs/*.md` là specification/nghiệp vụ.

Không tự ý thay đổi kiến trúc 8 service đã khóa.
