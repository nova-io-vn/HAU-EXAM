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
