> **Vị trí đặt file:** `backend/notification-service/docs/DATABASE.md`

# Notification Service --- Database

Database: `notification_db`.

Entity: - Notification - ScheduledNotification - optional
DeliveryAttempt

Notification: id, userId, type, title, content, referenceId,
referenceType, isRead, readAt, createdAt.

Scheduled: targetRole/targetFaculty, scheduledAt, status, creator,
timestamps.

Implemented tables: `notifications`, `scheduled_notifications`, and the durable consumer inbox `processed_events`.

`device_tokens` stores authenticated user device registrations: `user_id`,
`token`, `platform`, optional `device_identifier`, `active`, `created_at`, and
`updated_at`. Tokens are unique and invalid provider tokens are deactivated.

`email_settings` stores the runtime SMTP host, port, username, encrypted
password, sender identity, security mode, enabled flag, and timestamps. The
password is encrypted with AES-GCM using `EMAIL_SETTINGS_ENCRYPTION_KEY`; the
plaintext value is never returned by the API.

`contact_requests` stores public support requests, status, optional reply, and
reply timestamps.
