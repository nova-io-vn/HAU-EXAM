export const SMTP_PASSWORD_PLACEHOLDERS = new Set([
  "••••••••",
  "••••••••••••",
  "********",
]);

export const emptyEmailSettings = Object.freeze({
  smtpHost: "",
  smtpPort: 587,
  smtpUsername: "",
  smtpPassword: "",
  fromEmail: "",
  fromName: "HAU QM",
  security: "STARTTLS",
  enabled: false,
  passwordConfigured: false,
});

export function normalizeEmailSettings(data = {}) {
  return {
    ...emptyEmailSettings,
    ...data,
    smtpPassword: "",
    passwordConfigured: Boolean(data.passwordConfigured),
  };
}

export function updateEmailSetting(current, name, value) {
  const next = { ...current, [name]: value };
  const host = String(name === "smtpHost" ? value : current.smtpHost)
    .trim()
    .toLowerCase();
  if (host !== "smtp.gmail.com") return next;

  if (name === "smtpHost" && ["", null, 465, 587].includes(current.smtpPort)) {
    next.smtpPort = current.security === "SSL_TLS" ? 465 : 587;
  }
  if (name === "security") {
    if (value === "STARTTLS") next.smtpPort = 587;
    if (value === "SSL_TLS") next.smtpPort = 465;
  }
  return next;
}

export function validateEmailSettings(settings) {
  const host = String(settings.smtpHost || "").trim().toLowerCase();
  const port = Number(settings.smtpPort);
  const password = String(settings.smtpPassword || "").trim();
  if (host === "smtp.gmail.com" && port === 587 && settings.security === "SSL_TLS") {
    return "Gmail cổng 587 sử dụng STARTTLS. SSL/TLS sử dụng cổng 465.";
  }
  if (host === "smtp.gmail.com" && port === 465 && settings.security === "STARTTLS") {
    return "Gmail cổng 465 sử dụng SSL/TLS. STARTTLS sử dụng cổng 587.";
  }
  if (host === "smtp.gmail.com" && settings.security === "STARTTLS" && port !== 587) {
    return "Gmail STARTTLS sử dụng cổng 587.";
  }
  if (host === "smtp.gmail.com" && settings.security === "SSL_TLS" && port !== 465) {
    return "Gmail SSL/TLS sử dụng cổng 465.";
  }
  if (!settings.enabled) return "";
  if (!host || !Number.isInteger(port) || port < 1 || port > 65535 || !settings.fromEmail || !settings.security) {
    return "Cấu hình SMTP không hợp lệ.";
  }
  if (settings.security !== "NONE" && !settings.smtpUsername) {
    return "Email / tài khoản SMTP là bắt buộc.";
  }
  if (
    settings.security !== "NONE" &&
    !settings.passwordConfigured &&
    (!password || SMTP_PASSWORD_PLACEHOLDERS.has(password))
  ) {
    return "Mật khẩu SMTP là bắt buộc khi bật gửi email lần đầu.";
  }
  return "";
}

export function emailSettingsPayload(settings) {
  const password = String(settings.smtpPassword || "").trim();
  const payload = {
    smtpHost: String(settings.smtpHost || "").trim(),
    smtpPort: Number(settings.smtpPort),
    smtpUsername: String(settings.smtpUsername || "").trim(),
    fromEmail: String(settings.fromEmail || "").trim(),
    fromName: String(settings.fromName || "").trim(),
    security: settings.security,
    enabled: Boolean(settings.enabled),
  };
  if (password && !SMTP_PASSWORD_PLACEHOLDERS.has(password)) {
    payload.smtpPassword = password;
  }
  return payload;
}

export function comparableEmailSettings(settings) {
  return JSON.stringify(emailSettingsPayload({ ...settings, smtpPassword: "" }));
}
