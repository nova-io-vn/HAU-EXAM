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
