# 8. Bảo mật

Auth ký access token bằng RS256 và công bố public key qua `/.well-known/jwks.json`. Gateway và resource service xác thực bằng JWKS. Claim chính gồm `sub`, `lecturerCode`, `role`, `facultyId` nếu có, `jti`, `iat`, `exp`.

Ba role hợp lệ là `SYSTEM_ADMIN`, `SUBJECT_ADMIN`, `USER`. Role trong token lấy từ security snapshot đã đồng bộ, không phải giá trị do client gửi.

Gateway chỉ xác thực token và route; Question Service kiểm tra nghiệp vụ. SUBJECT_ADMIN của `CNTT` không được approve question thuộc khoa Kiến trúc; trường hợp này trả 403.

Các biện pháp khác: BCrypt, OTP Redis có TTL, request/domain validation, Gateway rate limit, `INTERNAL_SERVICE_TOKEN` cho internal API, secret qua environment/profile và global exception handler không trả stack trace. Không log password, access token, refresh token hoặc OTP.

Discrepancy tài liệu: root `AGENTS.md` còn một ví dụ tên `JWT_SECRET` ở phần hướng dẫn cũ; runtime source dùng RS256 key variables, không dùng biến đó.
# Bootstrap tài khoản quản trị ban đầu

Tài khoản quản trị hệ thống ban đầu được bootstrap từ biến môi trường khi triển
khai lần đầu. Password chỉ đi qua cấu hình runtime và được hash bằng password
encoder hiện tại; không hard-code vào Java/YAML, không log password/hash/token.
Auth Service phát event sang User Service để tạo profile, không truy cập chéo
database. Cơ chế kiểm tra theo lecturerCode/email và userId giúp restart hoặc
redelivery không tạo duplicate. Production phải cấp secret riêng và chỉ bật
bootstrap khi deployment cần cơ chế khởi tạo này.
