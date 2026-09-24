import assert from "node:assert/strict";
import test from "node:test";
import {
  emailSettingsPayload,
  normalizeEmailSettings,
  updateEmailSetting,
  validateEmailSettings,
} from "./smtpSettings.js";

const gmail = {
  smtpHost: "smtp.gmail.com",
  smtpPort: 587,
  smtpUsername: "sender@example.test",
  smtpPassword: "",
  passwordConfigured: true,
  fromEmail: "sender@example.test",
  fromName: "HAU QM",
  security: "STARTTLS",
  enabled: true,
};

test("GET settings keeps configured-password state without putting a secret in the field", () => {
  const result = normalizeEmailSettings({
    ...gmail,
    passwordConfigured: true,
  });
  assert.equal(result.passwordConfigured, true);
  assert.equal(result.smtpPassword, "");
});

test("save without a new password omits smtpPassword and UI-only fields", () => {
  const payload = emailSettingsPayload(gmail);
  assert.equal("smtpPassword" in payload, false);
  assert.equal("passwordConfigured" in payload, false);
});

test("password placeholders are never submitted", () => {
  for (const placeholder of ["••••••••", "••••••••••••", "********"]) {
    assert.equal(
      "smtpPassword" in emailSettingsPayload({ ...gmail, smtpPassword: placeholder }),
      false,
    );
  }
});

test("Gmail security changes select the matching recommended port", () => {
  assert.equal(updateEmailSetting(gmail, "security", "STARTTLS").smtpPort, 587);
  assert.equal(updateEmailSetting(gmail, "security", "SSL_TLS").smtpPort, 465);
});

test("Gmail 587 with SSL_TLS is rejected before an API call", () => {
  assert.equal(
    validateEmailSettings({ ...gmail, security: "SSL_TLS", smtpPort: 587 }),
    "Gmail cổng 587 sử dụng STARTTLS. SSL/TLS sử dụng cổng 465.",
  );
});
