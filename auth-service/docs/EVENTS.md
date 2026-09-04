> **Vị trí đặt file:** `backend/auth-service/docs/EVENTS.md`

# Auth Service --- Events

Produces: - `user.registration.requested` -
`password.reset.otp.requested`

Consumes: - `user.approved` - `user.rejected` - `user.status.changed`

Mọi event dùng envelope chung và correlation ID. Consumer phải
idempotent. OTP event không được log OTP ở plaintext; payload delivery
phải được bảo vệ và chỉ chứa dữ liệu tối thiểu cần thiết.
