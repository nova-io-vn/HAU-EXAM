> **Vị trí đặt file:** `docs/SECURITY.md`

# Security Baseline

> Bản giải thích tiếng Việt dùng cho bàn giao/báo cáo nằm tại [07-BAO-MAT.md](07-BAO-MAT.md).

## Authentication

JWT access/refresh token do Auth Service ký bằng RS256. Private key chỉ tồn tại
tại Auth Service và phải lấy từ secret/environment trong production. Auth expose
public JWKS tại `/.well-known/jwks.json`; Gateway và resource services chỉ nhận
public key qua `jwk-set-uri`. JWT gồm `sub`, `lecturerCode`, `role`, `facultyId`
khi có, `jti`, `iat`, `exp`; role/faculty lấy từ security snapshot đã đồng bộ.

-   Login bằng `lecturerCode + password`.
-   JWT Access Token + Refresh Token.
-   BCrypt cho password.
-   Tài khoản mới ở `PENDING_APPROVAL`.
-   Chỉ tài khoản hợp lệ/ACTIVE mới được đăng nhập.
-   OTP quên mật khẩu có TTL và chỉ dùng một lần.

## Authorization

Role: `SYSTEM_ADMIN`, `SUBJECT_ADMIN`, `USER`.

Ngoài role, các nghiệp vụ chuyên môn phải kiểm tra Faculty Data Scope.
`SUBJECT_ADMIN` khoa A không được duyệt dữ liệu khoa B.

## Input/Output

-   Validate request bằng Jakarta Validation.
-   Business validation ở Application/Domain.
-   Không expose JPA Entity.
-   Không trả stacktrace.
-   Không tin `userId`, `role`, `facultyId` do client tự gửi nếu thông
    tin đó phải lấy từ identity đã xác thực.

## Secrets

Secret đi qua environment variables/config secret store. `.env` thật
không commit Git.

## Logging

Không log: - password; - OTP; - access/refresh token; - API key; - SMTP
password; - DB password.

Dùng correlation ID để trace thay vì dữ liệu nhạy cảm.
