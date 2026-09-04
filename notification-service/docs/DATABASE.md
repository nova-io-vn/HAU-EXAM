> **Vị trí đặt file:** `backend/notification-service/docs/DATABASE.md`

# Notification Service --- Database

Database: `notification_db`.

Entity: - Notification - ScheduledNotification - optional
DeliveryAttempt

Notification: id, userId, type, title, content, referenceId,
referenceType, isRead, readAt, createdAt.

Scheduled: targetRole/targetFaculty, scheduledAt, status, creator,
timestamps.
