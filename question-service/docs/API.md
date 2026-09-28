> **Vị trí đặt file:** `backend/question-service/docs/API.md`

# Question Service --- API

Implemented under `/api/v1`: Question CRUD/search, submit, approve, reject,
request-revision and archive; plus Subject, Chapter and Topic CRUD. Search
supports faculty, subject, chapter, topic, difficulty, status, source,
createdBy, keyword, pagination, and allow-listed sorting. JWT `sub`, `role`,
and `facultyId` claims define ownership and faculty scope.

Nhóm API: - Question CRUD/search/filter. - `/questions/{id}/submit` -
`/questions/{id}/approve` - `/questions/{id}/reject` -
`/questions/{id}/request-revision` - `/questions/{id}/archive` -
Subject/Chapter/Topic CRUD phù hợp role.

Filter quan trọng: faculty, subject, chapter, topic, difficulty, status,
source, creator.

## Shared subject and lecturer scope

- `GET /api/v1/subjects` returns `managingFacultyId` and
  `participatingFacultyIds` for each canonical Subject.
- `POST|PUT /api/v1/subjects[/{id}]` accepts `code`, `name`,
  `managingFacultyId`, and `participatingFacultyIds`. `SYSTEM_ADMIN` may
  configure scope; a managing `SUBJECT_ADMIN` may maintain the Subject but
  cannot move its owner or expand its faculty scope.
- `POST /api/v1/subjects/{id}/lecturers` assigns `{userId}`. Only
  `SYSTEM_ADMIN` or the Subject's managing-faculty `SUBJECT_ADMIN` may call
  it. The target must be an active `USER` in an active participating scope.
- `DELETE /api/v1/subjects/{id}/lecturers/{userId}` removes an assignment.
- `GET /api/v1/subjects/{id}/lecturers` returns lecturer display profiles.
- `GET /api/v1/subjects/{id}/eligible-lecturers?keyword=` searches assignable
  lecturers by full name or lecturer code.

Semantic authorization errors include `SUBJECT_ACCESS_DENIED`,
`LECTURER_OUTSIDE_SCOPE`, `LECTURER_NOT_FOUND`, and
`SUBJECT_NOT_ASSIGNED`.
