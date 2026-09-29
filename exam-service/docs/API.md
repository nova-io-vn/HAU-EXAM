# Exam Service API

Exam Service is the source of truth for matrices, templates, persisted exams,
exam versions, and selected question references. All endpoints below require
`SUBJECT_ADMIN`; the service enforces the faculty claim.

- `GET/POST/PUT /api/v1/exam-matrices`: list, create, and update matrices.
- `POST /api/v1/exam-matrices/{id}/validate`: validate approved-question coverage.
- `GET /api/v1/exam-matrices/{id}/capacity?startCode=101&endCode=105`:
  analyze every matrix bucket against the currently available APPROVED bank and
  return exact shortages plus estimated cross-version reuse.
- `GET/POST /api/v1/exam-templates`: list and create templates.
- `POST /api/v1/exams/generate`: atomically persist one or more versions. In
  addition to exam metadata, the request accepts `startCode`, `endCode`,
  `shuffleQuestions`, `shuffleAnswers`, and `allowQuestionReplacement`.
  The inclusive range is bounded by `exam.generation.max-version-count`.
- `GET /api/v1/exams`: list persisted exams for the authenticated faculty.
- `GET /api/v1/exams/{id}`: return metadata, versions, and selected questions.
- `POST /api/v1/exams/{id}/versions`: persist a regenerated version.
- `GET /api/v1/exams/{id}/pdf?version=N&type=STUDENT_EXAM|ANSWER_KEY`:
  export one persisted version without regenerating it.
- `GET /api/v1/exams/{id}/pdf/all?type=STUDENT_EXAM|ANSWER_KEY`: export every
  persisted version in a ZIP archive.

Generation fails explicitly when the approved question bank cannot satisfy the
matrix. Exam Service stores question IDs, matrix-bucket IDs, question order,
generation seed, and stable option-ID display order. It never accesses the
Question database directly.
