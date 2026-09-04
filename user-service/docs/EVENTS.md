> **Vị trí đặt file:** `backend/user-service/docs/EVENTS.md`

# User Service --- Events

Consumes: - `user.registration.requested`

Produces: - `user.approved` - `user.rejected` - `user.role.changed` -
`user.status.changed` - `user.faculty.changed`

Registration consumer phải idempotent theo eventId/userId/lecturerCode
phù hợp.
