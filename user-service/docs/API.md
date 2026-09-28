> **Vị trí đặt file:** `backend/user-service/docs/API.md`

# User Service --- API

Nhóm API: - `/api/v1/users/me` - `/api/v1/users/{id}` -
`/api/v1/users` - `/api/v1/users/{id}/approve` -
`/api/v1/users/{id}/reject` - `/api/v1/users/{id}/role` -
`/api/v1/users/{id}/faculty` - `/api/v1/users/{id}/lock` -
`/api/v1/users/{id}/unlock`

Endpoint admin phải được bảo vệ bằng role và business rule.

## Contract implemented

| Method | Path | Access |
|---|---|---|
| GET | `/api/v1/users/me` | Authenticated user |
| PUT | `/api/v1/users/me` | Authenticated user; updates own profile only |
| GET | `/api/v1/users` | `SYSTEM_ADMIN` |
| GET | `/api/v1/users/{id}` | `SYSTEM_ADMIN` |
| POST | `/api/v1/users/{id}/approve` | `SYSTEM_ADMIN` |
| POST | `/api/v1/users/{id}/reject` | `SYSTEM_ADMIN` |
| PUT | `/api/v1/users/{id}/role` | `SYSTEM_ADMIN` |
| PUT | `/api/v1/users/{id}/faculty` | `SYSTEM_ADMIN` |
| POST | `/api/v1/users/{id}/lock` | `SYSTEM_ADMIN` |
| POST | `/api/v1/users/{id}/unlock` | `SYSTEM_ADMIN` |

`/me` derives the user id from the authenticated JWT `sub`; it does not accept a client-supplied user id.
### Assign faculty subject administrator

```http
PATCH /api/v1/faculties/{facultyId}/subject-admin

Internal service-authenticated lecturer directory endpoints:

- `GET /api/v1/internal/users/{id}/contact`
- `GET /api/v1/internal/users/lecturers?facultyId=...&keyword=...`

The lecturer directory returns only active `USER` profiles and supports
multiple repeated `facultyId` parameters. It exposes display metadata needed
by Question Service, including academic rank/degree, without exposing
credentials.
Authorization: Bearer <SYSTEM_ADMIN JWT>
Content-Type: application/json

{"userId":"<active-user-uuid>"}
```

`userId` may be `null` to remove the current assignment. The endpoint validates that the faculty is active and publishes the existing `user.role.changed` and `user.faculty.changed` events. The response contains derived `lecturerCount` and `subjectAdmins` data.

## Chat directory and public branding

- `GET /api/v1/users/me/chat-contacts` returns real display name, lecturer code, avatar,
  role, and faculty for authorized chat contacts. `USER` receives faculty
  `SUBJECT_ADMIN` and `SYSTEM_ADMIN`; `SUBJECT_ADMIN` receives faculty `USER`
  and `SYSTEM_ADMIN`; `SYSTEM_ADMIN` receives active users and subject admins.
- `GET /api/v1/public/system-branding` is unauthenticated and returns only
  `systemName`, `shortName`, `logoUrl`, and `faviconUrl`.
- `GET/PUT /api/v1/admin/platform/branding` requires `SYSTEM_ADMIN`.
- `POST /api/v1/admin/platform/branding/logo` and `/favicon` require
  `SYSTEM_ADMIN`, store the file through the existing image-storage adapter, and
  persist only its public URL.
