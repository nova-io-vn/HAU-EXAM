# HAU QM Telegram Notifications

Telegram is delivered by Notification Service only. Business services publish events through RabbitMQ; Notification Service fans them out to in-app, WebSocket, email and Telegram channels. No Telegram microservice is introduced.

## Production configuration

Configure secrets outside source control:

- `TELEGRAM_BOT_TOKEN`: Kubernetes Secret or deployment secret, never a frontend variable.
- `TELEGRAM_BOT_USERNAME`: configured bot username without the `@` prefix.
- `TELEGRAM_WEBHOOK_SECRET`: secret header value used by the webhook endpoint.
- `PUBLIC_APP_URL`: public HAU QM URL.

If an administrator saves a token through System Settings, Notification Service encrypts it server-side with the existing AES-GCM secret protector. Read APIs return only `tokenConfigured`; the plaintext token is never returned or logged.

## Linking an account

1. Open **User menu → Tích hợp Telegram**.
2. Select **Liên kết Telegram**.
3. Open the generated bot link and press **START**.
4. Notification Service validates the random, single-use token received in `/start`, reads Telegram's chat id, and stores the connection for the authenticated HAU user.
5. The token expires after ten minutes and cannot be reused.

Users never copy a chat id manually. They can send a test notification or unlink the connection from the same page.

## Preferences

SYSTEM_ADMIN can receive new-user, actionable, system and optional login events. SUBJECT_ADMIN can receive pending-question, resubmitted-question and subject notifications. Normal login-to-Telegram is off by default. USER accounts do not receive the Telegram administration screen.

## Webhook

Expose the gateway route `/api/v1/integrations/telegram/webhook` over HTTPS. Telegram must send the configured webhook secret header. The endpoint accepts only Telegram update data, validates the one-time `/start` token, and does not require a normal HAU JWT.

Successful login is intentionally audit-only and does not fan out to SYSTEM_ADMIN. Security-relevant login alerts should be introduced only when a reliable failed-login/lockout event contract is available. The optional `loginEvents` preference remains disabled by default.

Telegram delivery failures are isolated from the business operation. RabbitMQ redelivery is protected by the existing processed-event/idempotency handling, and delivery can be retried without creating a second business notification.

## Security rules

Never place a real bot token in source, documentation, tests, frontend bundles, logs or chat. Never include passwords, JWTs, refresh tokens, API keys, SMTP credentials or internal service tokens in Telegram or email messages.
