> **Vị trí đặt file:** `mobile/docs/SECURITY.md`

# Mobile Security

- Không lưu password.
- Không log OTP/token.
- Token dùng secure storage abstraction.
- Không commit secret.
- Không bypass TLS.
- Không disable SSL verification.
- Backend là authorization authority.
- Logout clear session local.
- Không tự thêm biometric/device attestation nếu chưa có requirement.
