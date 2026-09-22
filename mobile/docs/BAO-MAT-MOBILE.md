# Bảo mật Mobile

- Access/refresh token đi qua secure storage abstraction, không lưu password/OTP.
- Không log token, refresh token, password hoặc OTP.
- Refresh chỉ chạy single-flight; thất bại thì clear session và reset Login.
- Role guard không thay thế backend authorization; không coi faculty scope là client security.
- Camera/photo/file permission chỉ xin khi user sử dụng chức năng.
- File picker validate theo loại/kích thước backend hỗ trợ.
- OS push phân biệt với in-app và WebSocket.
- Secret production không đặt trong tracked `.env`.

OS push provider/token rotation có code nền và contract tương ứng, nhưng device/provider live validation là NOT TESTED.
