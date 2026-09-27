# HAU QM Telegram Notifications

Telegram is a delivery channel inside `notification-service`; there is no Telegram microservice.

## Production configuration

Configure these values through Kubernetes Secrets or the deployment environment:

- `TELEGRAM_BOT_TOKEN` — write-only bot credential; never commit or expose it.
- `TELEGRAM_BOT_USERNAME` — public bot username without `@`.
- `TELEGRAM_WEBHOOK_SECRET` — secret header value used to validate Telegram webhook requests.
- `TELEGRAM_API_URL` — normally `https://api.telegram.org`.
- `EMAIL_SETTINGS_ENCRYPTION_KEY` — AES key used for encrypted database secrets.

The SYSTEM_ADMIN may replace the bot token through the UI. The service stores it encrypted with AES-GCM, and GET responses only expose `tokenConfigured`; plaintext is never returned or logged. SUBJECT_ADMIN can link their own account and manage personal delivery preferences, but cannot change the global bot configuration.

## User linking

1. Open User Menu → Tích hợp Telegram.
2. Select Tạo liên kết Telegram.
3. Open HAU QM Bot and press START.
4. The one-time random token is validated by the webhook and mapped to the Telegram `chat.id`.
5. The token is consumed and expires after 10 minutes.

Users never copy or enter a chat ID. Unlink removes the connection and disables delivery.

## Delivery policy

Registration and important administrative events target SYSTEM_ADMIN. Submitted questions target SUBJECT_ADMIN in the question's faculty. Normal login events are available in-app and Telegram login delivery is off by default. Telegram failures are isolated from the business transaction; RabbitMQ retry/DLQ handles event processing and the inbox event ID prevents duplicate processing.

## Webhook

Expose `POST /api/v1/integrations/telegram/webhook` through the HTTPS gateway and configure Telegram to send the `X-Telegram-Bot-Api-Secret-Token` header. Requests without the configured secret are rejected before processing. The endpoint accepts `/start <one-time-token>` only and does not require a normal user JWT.

Successful login is intentionally audit-only and does not fan out to SYSTEM_ADMIN. Security-relevant login alerts should be introduced only when a reliable failed-login/lockout event contract is available. The optional `loginEvents` preference remains disabled by default.

No real bot token belongs in this document, source code, frontend, tests, or logs.
