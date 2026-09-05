# Exam Matrix and Exam UI

SUBJECT_ADMIN routes:
- `/exam-matrices`: faculty-scoped matrix list.
- `/exam-matrices/new`: create matrix.
- `/exam-matrices/:id`: distribution/detail and backend validation.
- `/exam-matrices/:id/edit`: edit matrix.
- `/exams/generate?matrixId=...`: synchronous generation (HTTP 201).
- `/exams/:id?version=N`: generated composition and immutable versions.
- `/exams`: generation entry and lookup of an existing exam by UUID.

The current backend has no exam-list or export endpoint. No fictitious list,
download link or client export is provided. All calls use the Gateway client.
Backend checks SUBJECT_ADMIN and JWT faculty; the UI never fetches all faculties
to enforce scope with client filtering.

Matrix cells use inline integer inputs. Totals update immediately; save checks
nonnegative counts, positive matching target, required catalog selection and
duplicate chapter/topic rows. Positive cells map to RuleRequest; zero cells do
not generate unnecessary zero-count selection rules. Backend is final validator.
Changing subject resets distribution; the form explains this next to the field.
Under the current backend implementation a null topic matches questions with no
topic, not all topics in a chapter; the UI labels this explicitly.

INSUFFICIENT_APPROVED_QUESTIONS currently contains chapter/topic/difficulty and
required/available counts in the domain error message. The UI extracts that
documented format and shows the rule and counts. If the message format differs,
it shows the returned message without inventing availability numbers.

Generation and version creation guard repeated clicks. New versions require
confirmation and retain existing backend versions. The UI displays all returned
question references in backend position order; it does not deduplicate or invent
selection rules. Optional question preview fetches the authorized Question API.
Exam Service persists logical references, so preview is current question content,
not a frozen historical content snapshot. This distinction is shown to the user.

No online examination, attempts, answers submission, countdown, scoring or
anti-cheat features are included.

Checks: `npm run lint`, `npm run build`, and
`node --test src/features/exams/model/*.test.js`.
Live verification should cover faculty 403, create/edit roundtrip, insufficient
approved questions, persisted versions and a question that becomes inaccessible.
