import assert from "node:assert/strict";
import test from "node:test";
import { getErrorMessage } from "./errorMessages.js";

const smtpMessages = {
  SMTP_CONFIGURATION_INVALID: "Cấu hình SMTP không hợp lệ.",
  SMTP_CONNECTION_FAILED: "Không thể kết nối tới máy chủ SMTP.",
  SMTP_AUTHENTICATION_FAILED: "SMTP từ chối thông tin đăng nhập.",
  SMTP_TLS_FAILED: "Không thể thiết lập kết nối bảo mật SMTP.",
  SMTP_CREDENTIAL_DECRYPTION_FAILED:
    "Không thể đọc thông tin xác thực SMTP đã lưu.",
  EMAIL_DELIVERY_DISABLED: "Chức năng gửi email hiện đang tắt.",
  EMAIL_SEND_FAILED: "Không thể gửi email.",
};

test("maps every SMTP backend error to a specific Vietnamese message", () => {
  for (const [code, message] of Object.entries(smtpMessages)) {
    assert.equal(getErrorMessage({ code }), message);
  }
});
