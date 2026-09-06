# 12. Quy trình nghiệp vụ

## Đăng ký và duyệt

```text
Register -> Auth tạo credential PENDING_APPROVAL
         -> user.registration.requested
         -> User tạo pending profile
SYSTEM_ADMIN approve -> user.approved -> Auth snapshot ACTIVE
                     -> Login được phép
```

Rejected/locked/pending không được login.

## Câu hỏi

```text
DRAFT -> PENDING_REVIEW -> APPROVED
              |-> NEED_REVISION -> DRAFT
              |-> REJECTED
```

USER chỉ sửa câu hỏi của mình theo rule service. SUBJECT_ADMIN review theo faculty scope.

## AI và Exam

AI trả 202/jobId, xử lý bất đồng bộ và tạo question `source=AI`, `status=DRAFT`; không tự approve. Exam Service validate matrix, chọn question đã APPROVED và tạo generated exam/version. Không có workflow thi online.

## Notification

Event domain được Notification Service lưu in-app, đẩy WebSocket hoặc gửi email tùy loại. WebSocket không phải OS push; mobile push token/provider có phần đã có trong source nhưng cần kiểm thử provider thật riêng.
