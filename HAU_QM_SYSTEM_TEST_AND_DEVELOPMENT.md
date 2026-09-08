# TÀI LIỆU KIỂM THỬ HỆ THỐNG HAU QM SYSTEM

## 1. Thông tin chung

**Tên hệ thống:** HAU QM System  
**Mục tiêu:** Hệ thống quản lý ngân hàng câu hỏi trắc nghiệm, hỗ trợ tạo câu hỏi bằng AI, quản lý Khoa – Môn học – Chương – Chủ đề, phê duyệt câu hỏi, ma trận đề và thông báo.

**Phạm vi:**
- Không phải hệ thống thi trực tuyến.
- Không bao gồm chức năng sinh viên làm bài, nộp bài, chấm điểm hoặc chống gian lận.
- Tập trung vào quản lý nội dung học thuật và quy trình tạo – duyệt – sử dụng câu hỏi.

---

## 2. Kiến trúc hệ thống cần kiểm thử

Hệ thống gồm 8 service:

1. Eureka Server
2. API Gateway
3. Auth Service
4. User Service
5. Question Service
6. Exam Service
7. AI Service
8. Notification Service

Hạ tầng:

- PostgreSQL
- Redis
- RabbitMQ
- WebSocket
- SMTP Email
- File/Object Storage
- ReactJS Web Frontend
- React Native Mobile Client

Luồng truy cập chính:

```text
Web / Mobile
    ↓
API Gateway
    ↓
Microservices
    ↓
PostgreSQL / Redis / RabbitMQ
```

---

## 3. Vai trò trong hệ thống

### 3.1. SYSTEM_ADMIN – Quản trị viên hệ thống

Chức năng chính:

- Quản lý Khoa
- Quản lý giảng viên
- Phê duyệt tài khoản đăng ký
- Từ chối tài khoản
- Gán Khoa cho giảng viên
- Gán vai trò USER / SUBJECT_ADMIN
- Khóa / mở khóa tài khoản
- Theo dõi thông báo hệ thống
- Quản lý một số cấu hình hệ thống

SYSTEM_ADMIN không phụ trách duyệt chuyên môn câu hỏi.

### 3.2. SUBJECT_ADMIN – Quản trị viên chuyên môn

Chức năng chính:

- Quản lý Môn học thuộc Khoa được phân công
- Quản lý Chương
- Quản lý Chủ đề
- Xem ngân hàng câu hỏi trong Khoa
- Xem câu hỏi chờ duyệt
- Phê duyệt câu hỏi
- Yêu cầu chỉnh sửa
- Từ chối câu hỏi
- Theo dõi độ bao phủ kiến thức
- Quản lý ma trận đề
- Sử dụng AI hỗ trợ tạo câu hỏi
- Nhận thông báo

SUBJECT_ADMIN chỉ được thao tác trong đúng Khoa của mình.

### 3.3. USER – Giảng viên

Chức năng chính:

- Quản lý câu hỏi của mình
- Tạo câu hỏi thủ công
- Tạo câu hỏi bằng AI
- Quản lý tài liệu
- Gửi câu hỏi phê duyệt
- Xem phản hồi
- Chỉnh sửa và gửi lại
- Xem ngân hàng câu hỏi được phép truy cập
- Xem ma trận đề
- Tạo phiên bản đề nếu được phép
- Nhận thông báo

---

## 4. Các chức năng chính cần kiểm thử

### 4.1. Đăng nhập

| ID | Trường hợp | Kết quả mong đợi |
|---|---|---|
| AUTH-01 | Đúng mã giảng viên + mật khẩu | Đăng nhập thành công |
| AUTH-02 | Sai mật khẩu | Trả lỗi xác thực |
| AUTH-03 | Sai mã giảng viên | Trả lỗi xác thực |
| AUTH-04 | Tài khoản PENDING_APPROVAL | Không được đăng nhập |
| AUTH-05 | Tài khoản REJECTED | Không được đăng nhập |
| AUTH-06 | Tài khoản LOCKED | Không được đăng nhập |
| AUTH-07 | Tài khoản ACTIVE | Đăng nhập thành công |
| AUTH-08 | JWT có đúng role và facultyId | Claims chính xác |

### 4.2. Refresh Token / Logout

| ID | Trường hợp | Kết quả mong đợi |
|---|---|---|
| AUTH-09 | Refresh token hợp lệ | Cấp token mới |
| AUTH-10 | Refresh token sai | Từ chối |
| AUTH-11 | Refresh token bị revoke | Từ chối |
| AUTH-12 | Logout | Refresh token bị thu hồi |
| AUTH-13 | Token dài hơn 72 byte | Không dùng BCrypt để hash token |

Yêu cầu:

- Password dùng BCrypt.
- Refresh token dùng SHA-256 hoặc cơ chế token fingerprint tương đương.
- Không lưu raw refresh token.

---

## 5. Đăng ký và phê duyệt tài khoản

Luồng nghiệp vụ:

```text
Đăng ký
  ↓
PENDING_APPROVAL
  ↓
SYSTEM_ADMIN chọn Khoa + Role
  ↓
Approve
  ↓
User Service cập nhật
  ↓
RabbitMQ
  ├── Auth Service cập nhật security snapshot
  └── Notification Service gửi thông báo + email
  ↓
ACTIVE
  ↓
Người dùng đăng nhập
```

| ID | Trường hợp | Kết quả mong đợi |
|---|---|---|
| REG-01 | Đăng ký hợp lệ | Tài khoản PENDING_APPROVAL |
| REG-02 | Approve không chọn Khoa | Không cho phép |
| REG-03 | Approve role USER | ACTIVE |
| REG-04 | Approve role SUBJECT_ADMIN + Faculty | ACTIVE |
| REG-05 | Gán SUBJECT_ADMIN không có Faculty | Không cho phép |
| REG-06 | Gán SYSTEM_ADMIN qua API thường | Không cho phép |
| REG-07 | Reject account | REJECTED |
| REG-08 | Approve thành công | Auth đồng bộ role/faculty/status |
| REG-09 | Approve thành công | Gửi email người đăng ký |
| REG-10 | Reject thành công | Gửi email thông báo |
| REG-11 | Event RabbitMQ bị gửi lại | Không gửi email trùng |

---

## 6. Quản lý Khoa

Chức năng:

- Thêm Khoa
- Sửa Khoa
- Bật / tắt trạng thái
- Tìm kiếm Khoa
- Phân trang
- Xem chi tiết Khoa

| ID | Trường hợp | Kết quả mong đợi |
|---|---|---|
| FAC-01 | SYSTEM_ADMIN tạo Khoa | Thành công |
| FAC-02 | Trùng mã Khoa | 409 hoặc lỗi domain phù hợp |
| FAC-03 | USER tạo Khoa | 403 |
| FAC-04 | SUBJECT_ADMIN tạo Khoa | 403 |
| FAC-05 | Search theo code/name | Trả kết quả đúng |
| FAC-06 | Filter active | Trả đúng dữ liệu |
| FAC-07 | Disable Khoa | Trạng thái cập nhật |
| FAC-08 | Gán user vào Khoa inactive | Không cho phép |

---

## 7. Quản lý giảng viên

Chức năng:

- Danh sách giảng viên
- Search theo mã GV, họ tên, email
- Filter theo Khoa
- Filter theo Role
- Filter theo Status
- Gán / đổi Khoa
- Chuyển USER ↔ SUBJECT_ADMIN
- Lock / Unlock

| ID | Trường hợp | Kết quả mong đợi |
|---|---|---|
| USER-01 | Search keyword | Search server-side chính xác |
| USER-02 | Filter Faculty | Trả đúng user |
| USER-03 | Filter Role | Trả đúng role |
| USER-04 | Kết hợp filter | Kết quả đúng |
| USER-05 | Gán Faculty | User + Auth đồng bộ |
| USER-06 | USER → SUBJECT_ADMIN | Role được cập nhật |
| USER-07 | SUBJECT_ADMIN → USER | Role được cập nhật |
| USER-08 | Lock account | Không đăng nhập được |
| USER-09 | Unlock | Đăng nhập được nếu ACTIVE |

---

## 8. Quản lý Môn học

Môn học thuộc Question Service.

Chức năng:

- Thêm Môn học
- Sửa Môn học
- Bật / tắt trạng thái
- Search
- Chỉ quản lý Môn thuộc Faculty của SUBJECT_ADMIN

| ID | Trường hợp | Kết quả mong đợi |
|---|---|---|
| SUB-01 | SUBJECT_ADMIN tạo Subject | Gắn Faculty từ JWT |
| SUB-02 | Client gửi facultyId Khoa khác | Không tin dữ liệu client |
| SUB-03 | Update Subject cùng Faculty | Thành công |
| SUB-04 | Update Subject Khoa khác | 403 |
| SUB-05 | USER tạo Subject | 403 |
| SUB-06 | Search Subject | Kết quả đúng |

---

## 9. Cấu trúc kiến thức

Cấu trúc:

```text
Faculty
  ↓
Subject
  ↓
Chapter
  ↓
Topic
```

Yêu cầu kiểm thử:

- Chapter phải thuộc Subject.
- Topic phải thuộc Chapter.
- SUBJECT_ADMIN chỉ thao tác trong Faculty của mình.
- Không được tạo quan hệ taxonomy sai.

| ID | Trường hợp | Kết quả mong đợi |
|---|---|---|
| TAX-01 | Tạo Chapter hợp lệ | Thành công |
| TAX-02 | Tạo Topic hợp lệ | Thành công |
| TAX-03 | Topic thuộc Chapter sai | Validation fail |
| TAX-04 | Chapter thuộc Subject Khoa khác | 403 |
| TAX-05 | USER quản lý taxonomy | 403 |

---

## 10. Câu hỏi

Trạng thái:

```text
DRAFT
  ↓
PENDING_REVIEW
  ├── APPROVED
  ├── NEED_REVISION
  └── REJECTED
```

Chức năng:

- Tạo câu hỏi
- Sửa câu hỏi
- Lưu nháp
- Gửi phê duyệt
- Xem câu hỏi của tôi
- Search/filter/pagination
- Xem trạng thái

| ID | Trường hợp | Kết quả mong đợi |
|---|---|---|
| Q-01 | USER tạo câu hỏi | DRAFT |
| Q-02 | Subject đúng Faculty | Thành công |
| Q-03 | Subject Khoa khác | Không cho phép |
| Q-04 | Chapter không thuộc Subject | Validation fail |
| Q-05 | Topic không thuộc Chapter | Validation fail |
| Q-06 | USER submit DRAFT | PENDING_REVIEW |
| Q-07 | NEED_REVISION gửi lại | PENDING_REVIEW |
| Q-08 | Search My Questions | Server-side |
| Q-09 | Statistics USER | Chỉ câu của chính user |
| Q-10 | Statistics SUBJECT_ADMIN | Chỉ Faculty của mình |
| Q-11 | Statistics SYSTEM_ADMIN | Toàn hệ thống |

---

## 11. Phê duyệt câu hỏi

| ID | Trường hợp | Kết quả mong đợi |
|---|---|---|
| REV-01 | SUBJECT_ADMIN cùng Faculty approve | APPROVED |
| REV-02 | SUBJECT_ADMIN khác Faculty | 403 |
| REV-03 | USER approve | 403 |
| REV-04 | Request Revision | NEED_REVISION |
| REV-05 | Reject | REJECTED |
| REV-06 | Revision có comment | Lưu được phản hồi |
| REV-07 | Approve | Thông báo tác giả |
| REV-08 | Approve | Email gửi đúng tác giả |
| REV-09 | Reject | Email/thông báo đúng tác giả |
| REV-10 | Event duplicate | Không gửi notification/email trùng |

---

## 12. AI Service

Chức năng:

- Upload tài liệu
- Tạo AI job
- Theo dõi job
- Sinh câu hỏi
- Lưu câu hỏi AI dưới dạng DRAFT

Trạng thái:

```text
PENDING
PROCESSING
COMPLETED
FAILED
```

| ID | Trường hợp | Kết quả mong đợi |
|---|---|---|
| AI-01 | Tạo AI job | HTTP 202 + jobId |
| AI-02 | Job pending | Hiển thị PENDING |
| AI-03 | Processing | PROCESSING |
| AI-04 | Thành công | COMPLETED |
| AI-05 | Lỗi | FAILED |
| AI-06 | AI sinh câu hỏi | Question DRAFT |
| AI-07 | AI question tự approve | Không được phép |
| AI-08 | Taxonomy không hợp lệ | Từ chối import |

---

## 13. Ma trận đề và phiên bản đề

Chức năng:

- Tạo ma trận
- Cấu hình số câu
- Cấu hình độ khó
- Cấu hình Chapter/Topic
- Sinh phiên bản đề
- Chỉ dùng câu hỏi APPROVED

| ID | Trường hợp | Kết quả mong đợi |
|---|---|---|
| EX-01 | Tạo Matrix hợp lệ | Thành công |
| EX-02 | Distribution sai | Validation fail |
| EX-03 | Generate exam | Chỉ dùng APPROVED |
| EX-04 | Question DRAFT được chọn | Không được phép |
| EX-05 | Faculty khác | 403 |
| EX-06 | Sinh phiên bản đề | Thành công |

---

## 14. Notification Service

Kênh:

- IN_APP
- WEBSOCKET
- EMAIL

Các thông báo chính:

- Tài khoản được phê duyệt
- Tài khoản bị từ chối
- Vai trò thay đổi
- Khoa thay đổi
- Tài khoản bị khóa / mở khóa
- Câu hỏi được phê duyệt
- Câu hỏi bị từ chối
- Yêu cầu chỉnh sửa
- AI hoàn tất

Kiểm thử:

- Persist notification trước realtime.
- Không gửi duplicate event.
- WebSocket đúng user.
- Email đúng recipient.
- SMTP lỗi không rollback nghiệp vụ chính.
- Không log password/token/OTP.

---

## 15. Kiểm thử giao diện Web

### Kiểm tra chung

- Sidebar đúng role.
- Không hiển thị menu role khác.
- Chỉ có một menu active.
- Mã giảng viên không bị hiển thị trùng.
- Profile hiển thị đúng fullName / role / Faculty.
- Icon thống nhất.
- Không còn icon ký tự D/S/K/Q.
- Giao diện tiếng Việt.
- Không còn link tím/gạch chân mặc định.
- Loading/Empty/Error đầy đủ.
- Không hard-code số liệu Stitch.

### SYSTEM_ADMIN

Kiểm tra:

- Dashboard
- Quản lý Khoa
- Quản lý giảng viên
- Tài khoản chờ duyệt
- Thông báo
- Cài đặt
- Trợ giúp

### SUBJECT_ADMIN

Kiểm tra:

- Dashboard chuyên môn
- Môn học
- Cấu trúc kiến thức
- Ngân hàng câu hỏi
- Câu hỏi chờ duyệt
- Độ bao phủ
- Ma trận đề
- AI
- Notification
- Help

### USER

Kiểm tra:

- Dashboard
- Câu hỏi của tôi
- Tạo câu hỏi
- Ngân hàng câu hỏi
- Tài liệu
- AI
- Ma trận đề
- Phiên bản đề
- Notification
- Help

---

## 16. Kiểm thử Search / Filter / Pagination

Các màn cần kiểm tra:

- Faculty
- Lecturer
- Pending Approval
- Subject
- Question Bank
- My Questions
- Review Queue
- Documents
- Exam Matrix

Yêu cầu:

```text
Search
→ debounce
→ page = 0
→ API
→ DB query
```

Không được:

```text
fetch page 1
→ frontend .filter()
```

nếu backend đang phân trang.

---

## 17. Kiểm thử bảo mật

| Case | Expected |
|---|---|
| Không JWT | 401 |
| Sai Role | 403 |
| Sai Faculty | 403 |
| Resource không tồn tại | 404 |
| Duplicate code | 409 |
| Invalid input | 400 |

Kiểm tra thêm:

- USER không tự đổi role.
- USER không tự đổi Faculty.
- SUBJECT_ADMIN không quản lý Faculty khác.
- Client không được tự gửi facultyId để bypass quyền.
- Gateway không tin identity header do browser gửi.
- Không expose password hash/token/OTP.

---

## 18. Kiểm thử RabbitMQ

Kiểm tra:

- Exchange tồn tại.
- Queue tồn tại.
- Binding đúng routing key.
- Consumer ACK sau xử lý thành công.
- Retry hợp lý.
- DLQ hoạt động.
- Duplicate event không xử lý lần hai.

Các event chính:

```text
user.registration.requested
user.approved
user.rejected
user.role.changed
user.faculty.changed
user.status.changed

question.submitted
question.approved
question.rejected
question.revision.requested

ai.generate.requested
ai.generation.completed
ai.generation.failed
```

---

## 19. Kiểm thử Docker Runtime

Sau khi source-level test pass:

```bash
docker compose build
docker compose up -d
docker compose ps
```

Kiểm tra:

- Eureka UP
- Gateway UP
- Auth UP
- User UP
- Question UP
- Exam UP
- AI UP
- Notification UP
- PostgreSQL healthy
- Redis healthy
- RabbitMQ healthy

Không chỉ kiểm tra container `Up`; phải test nghiệp vụ thật qua Gateway.

---

## 20. Luồng kiểm thử tích hợp quan trọng nhất

### Flow A – Đăng ký

```text
Register
→ Pending
→ Admin chọn Khoa + Role
→ Approve
→ Auth ACTIVE
→ Notification
→ Email
→ Login
```

### Flow B – Đồng bộ role

```text
USER
→ Admin chuyển SUBJECT_ADMIN
→ RabbitMQ
→ Auth security snapshot
→ logout/login
→ JWT mới đúng role/faculty
```

### Flow C – Question

```text
USER tạo câu hỏi
→ DRAFT
→ Submit
→ PENDING_REVIEW
→ SUBJECT_ADMIN cùng Khoa review
→ APPROVED
→ Notification
→ Email tác giả
```

### Flow D – Revision

```text
PENDING_REVIEW
→ NEED_REVISION
→ USER sửa
→ resubmit
→ PENDING_REVIEW
→ APPROVED
```

---

## 21. Các chức năng hiện tại cần hoàn thiện thêm

### 21.1. Dashboard tổng hợp

Có thể bổ sung aggregate API để frontend không phải gọi quá nhiều API.

Đề xuất:

- SYSTEM_ADMIN dashboard aggregate
- SUBJECT_ADMIN dashboard aggregate
- Faculty detail aggregate

Ví dụ dữ liệu:

- tổng user
- tổng Faculty
- pending account
- số SUBJECT_ADMIN
- tổng question
- phân bố question theo Subject
- phân bố user theo Faculty

### 21.2. Knowledge Coverage nâng cao

Hiện không nên tạo phần trăm độ bao phủ nếu chưa có target thật.

Cần phát triển:

- Target question count theo Chapter
- Target theo Topic
- Target theo Difficulty
- Kết nối với Exam Matrix

Sau đó mới tính:

```text
coverage = current / target
```

### 21.3. Dashboard biểu đồ

Có thể phát triển thêm:

SYSTEM_ADMIN:

- Số giảng viên theo Khoa
- Account theo status
- Question theo Khoa

SUBJECT_ADMIN:

- Question theo Subject
- Coverage theo Chapter
- Difficulty distribution

USER:

- Question theo Status
- Số câu hỏi theo Subject
- Lịch sử đóng góp theo thời gian

Tất cả phải lấy dữ liệu thật.

### 21.4. Audit Log

Có thể phát triển:

- Admin đổi Role
- Admin đổi Faculty
- Approve / Reject account
- Lock / Unlock
- Approve Question
- Reject Question
- Revision request
- Matrix changes

Audit Log nên lưu:

```text
actor
action
target
timestamp
correlationId
metadata an toàn
```

Không lưu password/token.

### 21.5. Email Outbox

Hiện email có thể dùng RabbitMQ retry.

Có thể phát triển thêm:

- Email outbox table
- delivery status
- retry count
- failed reason
- resend

### 21.6. Notification Preferences

Cho phép người dùng chọn:

- Email notification
- In-app notification
- Một số loại notification được bật/tắt

Các notification bảo mật quan trọng không nên cho tắt hoàn toàn.

### 21.7. Export

Có thể phát triển:

- Export Question Bank Excel
- Export Matrix Excel
- Export Exam PDF
- Export Exam Word
- Export statistics

### 21.8. Version History của Question

Có thể thêm:

- Version 1
- Version 2
- Review changes
- Diff giữa các lần sửa
- Ai chỉnh sửa
- Thời gian chỉnh sửa

### 21.9. AI nâng cao

Có thể phát triển:

- Phân loại độ khó
- Gợi ý Topic
- Gợi ý Chapter
- Kiểm tra câu hỏi tương tự
- Gợi ý cải thiện distractor
- Phân tích độ phủ
- Chatbot hỗ trợ tài liệu

AI không được tự động approve câu hỏi.

### 21.10. Mobile App

React Native có thể phát triển thêm:

- Notification realtime
- Xem question status
- Duyệt nhanh câu hỏi
- Upload tài liệu
- Theo dõi AI job

Các tác vụ form lớn như Matrix nên ưu tiên Web.

---

## 22. Các chức năng không thuộc phạm vi hiện tại

Không phát triển thành hệ thống thi trực tuyến nếu chưa đổi đề tài.

Không cần:

- Sinh viên làm bài
- Countdown
- Submit bài thi
- Chấm điểm
- Anti-cheat
- Camera monitoring
- Student exam analytics

---

## 23. Tiêu chí hoàn thành hệ thống

Hệ thống được xem là sẵn sàng demo khi:

- Login hoạt động.
- Refresh token hoạt động.
- Registration Approval hoạt động.
- Email approval hoạt động.
- Faculty CRUD hoạt động.
- User/Role/Faculty đồng bộ.
- Subject/Chapter/Topic hoạt động.
- Question workflow hoạt động.
- Faculty scope hoạt động.
- AI job hoạt động.
- Exam Matrix hoạt động.
- Notification hoạt động.
- Search/filter/pagination hoạt động.
- Web UI đúng role.
- Không còn dữ liệu mock production.
- Backend tests pass.
- Frontend lint/build pass.
- Playwright pass.
- Docker runtime test pass.

---

## 24. Checklist demo cuối

```text
[ ] SYSTEM_ADMIN login
[ ] Tạo Khoa
[ ] Search Khoa
[ ] Đăng ký account mới
[ ] Admin gán Khoa
[ ] Admin approve
[ ] Email approval nhận được
[ ] User login
[ ] Tạo Subject bằng SUBJECT_ADMIN
[ ] Tạo Chapter
[ ] Tạo Topic
[ ] USER tạo Question
[ ] Submit Question
[ ] SUBJECT_ADMIN thấy Review Queue
[ ] Approve Question
[ ] USER nhận Notification
[ ] USER nhận Email
[ ] Test NEED_REVISION
[ ] Test REJECTED
[ ] Test AI Job
[ ] Test Matrix
[ ] Test Exam Version
[ ] Test Search
[ ] Test Faculty Scope 403
[ ] Test USER không approve
[ ] Test Help
[ ] Test Notification Bell
[ ] Test responsive cơ bản
```

---

## 25. Kết luận

HAU QM System hiện tập trung vào ba trụ cột chính:

1. **Quản lý học thuật:** Khoa → Môn → Chương → Chủ đề → Câu hỏi.
2. **Quy trình kiểm duyệt:** Giảng viên tạo → Quản trị chuyên môn phê duyệt.
3. **Hỗ trợ thông minh:** RabbitMQ, AI, Notification, Email và realtime WebSocket.

Các chức năng phát triển thêm nên ưu tiên Dashboard aggregate, Audit Log, Coverage chuẩn theo Matrix, Email Outbox và Question Version History thay vì mở rộng sang nghiệp vụ thi trực tuyến.
