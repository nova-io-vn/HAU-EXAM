# Question Review Workspace

- `/review`: SUBJECT_ADMIN only; paginated `GET /api/v1/questions` with `status=PENDING_REVIEW`, keyword, source and oldest-created-first sorting. Backend applies faculty scope. No client-side faculty filtering.
- `/review/:id`: fetches the authorized detail. Includes content, images, correct options, catalog labels (ID fallback), source, author ID, faculty and review history.
- Approve confirms the decision; reject and revision require a nonblank comment, sent as `{reason}` to the existing action endpoints. Pending requests disable submission. The returned question supplies the new status and history.
- 403 replaces the workspace with Forbidden; 401, 404, 409 and transient errors have separate messages. A failed action is never automatically retried. Reload before making a new decision.
- SYSTEM_ADMIN and USER cannot access review routes. Visible actions also require matching faculty and PENDING_REVIEW; backend authorization remains authoritative.

## Contract limitations

QuestionResponse currently has no explanation, author display name, or detailed AI metadata. The workspace explicitly shows unavailable explanation/metadata and labels author IDs; it does not fabricate these fields or request administrative user lists.

## Verification

Run `npm run lint`, `npm run build`, and `node --test src/features/questions/model/*.test.js`.

With the running backend, verify a SUBJECT_ADMIN can review their faculty's pending question, another faculty's direct URL returns Forbidden, whitespace-only comments cannot submit, cancel sends no action, successful decisions update history and leave the queue, and concurrent review returns an error requiring reload. Verify SYSTEM_ADMIN cannot open either review route. Check keyboard dialog navigation and mobile layout.
