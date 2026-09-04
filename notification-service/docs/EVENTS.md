> **Vị trí đặt file:** `backend/notification-service/docs/EVENTS.md`

# Notification Service --- Events

Consumes các event cần thông báo: - password reset OTP request -
registration/approval status - question
submitted/approved/rejected/revision - AI completed/failed - exam
generated nếu cần

Consumer quyết định channel theo notification policy. Không assume mọi
event đều gửi email.
