> **Vị trí đặt file:** `backend/user-service/docs/EVENTS.md`

# User Service --- Events

Consumes: - `user.registration.requested`

Produces: - `user.approved` - `user.rejected` - `user.role.changed` -
`user.status.changed` - `user.faculty.changed`

Registration consumer phải idempotent theo eventId/userId/lecturerCode
phù hợp.

## RabbitMQ contract implemented

- Consume from `auth.exchange` / `user.registration.requested` using durable queue `user.registration.requested.queue`.
- Retry queue: `user.registration.requested.retry.queue`, finite retry (3 attempts) with configurable delay.
- Dead-letter queue: `user.registration.requested.dlq`.
- Publish produced events to topic exchange `user.exchange` with the routing keys listed above.

All messages use the common envelope: `eventId`, `eventType`, `correlationId`, `occurredAt`, `version`, `payload`.

Registration payload: `userId`, `lecturerCode`, `fullName`, `dateOfBirth`, `phone`, `email`, `address`, `avatar`, `facultyId`.

Produced user-change payload: `userId`, `lecturerCode`, `role`, `facultyId`, `status`.
