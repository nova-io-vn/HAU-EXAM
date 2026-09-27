# Exam Service API

Exam Service is the source of truth for matrices, templates, persisted exams,
exam versions, and selected question references. All endpoints below require
`SUBJECT_ADMIN`; the service enforces the faculty claim.

- `GET/POST/PUT /api/v1/exam-matrices`: list, create, and update matrices.
- `POST /api/v1/exam-matrices/{id}/validate`: validate approved-question coverage.
- `GET/POST /api/v1/exam-templates`: list and create templates.
- `POST /api/v1/exams/generate`: persist an exam and its first version. The request
  includes `name`, `examCode`, `durationMinutes`, `matrixId`, and optional
  `templateId`; the response contains the persisted exam `id`.
- `GET /api/v1/exams`: list persisted exams for the authenticated faculty.
- `GET /api/v1/exams/{id}`: return metadata, versions, and selected questions.
- `POST /api/v1/exams/{id}/versions`: persist a regenerated version.
- `GET /api/v1/exams/{id}/pdf?version=N`: export a persisted version
  as PDF.

Generation fails explicitly when the approved question bank cannot satisfy the
matrix. Exam Service stores logical question references only and never accesses
the Question database directly.
